package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.ManifestReader;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * Responsible for all things related to the manifest during verification.
 */
public class PayloadVerifier {
  private static final Logger logger = LoggerFactory.getLogger(PayloadVerifier.class);

  private final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  private final ExecutorService executor;
  
  /**
   * Create a PayloadVerifier using a cached thread pool and the 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping} mapping
   */
  public PayloadVerifier(){
    this(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), Executors.newCachedThreadPool());
  }

  /**
   * Create a PayloadVerifier using a cached thread pool and a custom mapping
   */
  public PayloadVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping) {
    this(nameMapping, Executors.newCachedThreadPool());
  }
  
  /**
   * Create a PayloadVerifier using a custom thread pool and the 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping} mapping
   */
  public PayloadVerifier(final ExecutorService executor) {
    this(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), executor);
  }
  
  /**
   * Create a PayloadVerifier using a custom thread pool and a custom mapping
   */
  public PayloadVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping, final ExecutorService executor) {
    this.nameMapping = nameMapping;
    this.executor = executor;
  }
  
  //right before this object is garbage collected, shutdown the thread pool so the resource isn't leaked
  @Override
  protected void finalize() throws Throwable {
    try {
        executor.shutdown();
    } finally {
        super.finalize();
    }
  }

  /**
   * Verify that all the files in the payload directory are listed in the manifest and 
   * all files listed in the manifests exist.
   * 
   * @param bag the bag to check to check
   * @param ignoreHiddenFiles to ignore hidden files unless they are specifically listed in a manifest
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException the path in the manifest was specifically crafted to cause harm
   * @throws UnsupportedAlgorithmException if the algorithm used for the manifest is unsupported
   * @throws InvalidBagitFileFormatException if any of the manifests don't conform to the bagit specification
   * @throws FileNotInPayloadDirectoryException if a file is listed in a manifest but doesn't exist in the payload directory
   * @throws InterruptedException if a thread is interrupted while doing work
   */
  public void verifyPayload(final Bag bag, final boolean ignoreHiddenFiles)
      throws IOException, MaliciousPathException, UnsupportedAlgorithmException, 
      InvalidBagitFileFormatException, FileNotInPayloadDirectoryException, InterruptedException {
    
    final Set<Path> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    checkAllFilesListedInManifestExist(allFilesListedInManifests);

    if (bag.getVersion().isOlder(new Version(1, 0))) {
      checkAllFilesInPayloadDirAreListedInAtLeastOneAManifest(allFilesListedInManifests, PathUtils.getDataDir(bag), ignoreHiddenFiles);
    } else {
      CheckAllFilesInPayloadDirAreListedInAllManifests(bag.getPayLoadManifests(), PathUtils.getDataDir(bag), ignoreHiddenFiles);
    }
  }

  /*
   * get all the files listed in all the manifests
   */
  private Set<Path> getAllFilesListedInManifests(final Bag bag)
      throws IOException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException {
    logger.debug("Getting all files listed in the manifest(s)");
    final Set<Path> filesListedInManifests = new HashSet<>();

    try(DirectoryStream<Path> directoryStream = 
        Files.newDirectoryStream(PathUtils.getBagitDir(bag.getVersion(), bag.getRootDir()))){
      for (final Path path : directoryStream) {
        final String filename = PathUtils.getFilename(path);
        if (filename.startsWith("tagmanifest-") || filename.startsWith("manifest-")) {
          logger.debug("Getting files and checksums listed in [{}]", path);
          final Manifest manifest = ManifestReader.readManifest(nameMapping, path, bag.getRootDir(),
              bag.getFileEncoding());
          filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
        }
      }
    }

    return filesListedInManifests;
  }

  /*
   * Make sure all the listed files actually exist
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void checkAllFilesListedInManifestExist(final Set<Path> files)
      throws FileNotInPayloadDirectoryException, InterruptedException {//TODO
    final CountDownLatch latch = new CountDownLatch(files.size());
    final List<Path> missingFiles = new ArrayList<>();

    logger.debug("Checking if all files listed in the manifest(s) exist");
    for (final Path file : files) {
      executor.execute(new CheckIfFileExistsTask(file, missingFiles, latch));
    }

    latch.await();

    if (!missingFiles.isEmpty()) {
      throw new FileNotInPayloadDirectoryException(
          "Manifest(s) contains file(s) " + missingFiles + " but they don't exist!");
    }
  }

  /*
   * Make sure all files in the directory are in at least 1 manifest
   */
  private void checkAllFilesInPayloadDirAreListedInAtLeastOneAManifest(final Set<Path> filesListedInManifests,
      final Path payloadDir, final boolean ignoreHiddenFiles) throws IOException {
    logger.debug("Checking if all payload files (files in {} dir) are listed in at least one manifest", payloadDir);
    if (Files.exists(payloadDir)) {
      Files.walkFileTree(payloadDir,
          new PayloadFileExistsInAtLeastOneManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }

  /*
   * as per the bagit-spec 1.0+ all files have to be listed in all manifests
   */
  private void CheckAllFilesInPayloadDirAreListedInAllManifests(final Set<Manifest> payLoadManifests,
      final Path payloadDir, final boolean ignoreHiddenFiles) throws IOException {
    logger.debug("Checking if all payload files (files in {} dir) are listed in all manifests", payloadDir);
    if (Files.exists(payloadDir)) {
      Files.walkFileTree(payloadDir, new PayloadFileExistsInAllManifestsVistor(payLoadManifests, ignoreHiddenFiles));
    }
  }

  public BagitAlgorithmNameToSupportedAlgorithmMapping getNameMapping() {
    return nameMapping;
  }
}
