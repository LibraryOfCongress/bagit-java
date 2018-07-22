package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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
public class ManifestVerifier implements AutoCloseable{
  private static final Logger logger = LoggerFactory.getLogger(ManifestVerifier.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

  private transient final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  private transient final ExecutorService executor;
  
  /**
   * Create a PayloadVerifier using a cached thread pool and the 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping} mapping
   */
  public ManifestVerifier(){
    this(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }

  /**
   * Create a PayloadVerifier using a cached thread pool and a custom mapping
   * 
   * @param nameMapping the mapping between BagIt algorithm name and the java supported algorithm
   */
  public ManifestVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping) {
    this(nameMapping, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }
  
  /**
   * Create a PayloadVerifier using a custom thread pool and the 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping} mapping
   * 
   * @param executor the thread pool to use when doing work
   */
  public ManifestVerifier(final ExecutorService executor) {
    this(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), executor);
  }
  
  /**
   * Create a PayloadVerifier using a custom thread pool and a custom mapping
   * 
   * @param nameMapping the mapping between BagIt algorithm name and the java supported algorithm
   * @param executor the thread pool to use when doing work
   */
  public ManifestVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping, final ExecutorService executor) {
    this.nameMapping = nameMapping;
    this.executor = executor;
  }
  
  @Override
  public void close() throws SecurityException{
    //shutdown the thread pool so the resource isn't leaked
    executor.shutdown();
  }

  /**
   * Verify that all the files in the payload directory are listed in the payload manifest and 
   * all files listed in all manifests exist.
   * 
   * @param bag the bag to check to check
   * @param ignoreHiddenFiles to ignore hidden files unless they are specifically listed in a manifest
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException the path in the manifest was specifically crafted to cause harm
   * @throws UnsupportedAlgorithmException if the algorithm used for the manifest is unsupported
   * @throws InvalidBagitFileFormatException if any of the manifests don't conform to the bagit specification
   * @throws FileNotInPayloadDirectoryException if a file is listed in a manifest but doesn't exist in the payload directory
   * @throws InterruptedException if a thread is interrupted while doing work
   */
  public void verifyManifests(final Bag bag, final boolean ignoreHiddenFiles)
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
    logger.debug(messages.getString("all_files_in_manifests"));
    final Set<Path> filesListedInManifests = new HashSet<>();

    try(DirectoryStream<Path> directoryStream = 
        Files.newDirectoryStream(PathUtils.getBagitDir(bag.getVersion(), bag.getRootDir()))){
      for (final Path path : directoryStream) {
        final String filename = PathUtils.getFilename(path);
        if (filename.startsWith("tagmanifest-") || filename.startsWith("manifest-")) {
          logger.debug(messages.getString("get_listing_in_manifest"), path);
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
  private void checkAllFilesListedInManifestExist(final Set<Path> files) throws FileNotInPayloadDirectoryException, InterruptedException {
    final CountDownLatch latch = new CountDownLatch(files.size());
    final Set<Path> missingFiles = new ConcurrentSkipListSet<>();

    logger.info(messages.getString("check_all_files_in_manifests_exist"));
    for (final Path file : files) {
      executor.execute(new CheckIfFileExistsTask(file, missingFiles, latch));
    }

    latch.await();

    if (!missingFiles.isEmpty()) {
      final String formattedMessage = messages.getString("missing_payload_files_error");
      throw new FileNotInPayloadDirectoryException(MessageFormatter.format(formattedMessage, missingFiles).getMessage());
    }
  }

  /*
   * Make sure all files in the directory are in at least 1 manifest
   */
  private static void checkAllFilesInPayloadDirAreListedInAtLeastOneAManifest(final Set<Path> filesListedInManifests,
      final Path payloadDir, final boolean ignoreHiddenFiles) throws IOException {
    logger.debug(messages.getString("checking_file_in_at_least_one_manifest"), payloadDir);
    if (Files.exists(payloadDir)) {
      Files.walkFileTree(payloadDir,
          new PayloadFileExistsInAtLeastOneManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }

  /*
   * as per the bagit-spec 1.0+ all files have to be listed in all manifests
   */
  private static void CheckAllFilesInPayloadDirAreListedInAllManifests(final Set<Manifest> payLoadManifests,
      final Path payloadDir, final boolean ignoreHiddenFiles) throws IOException {
    logger.debug(messages.getString("checking_file_in_all_manifests"), payloadDir);
    if (Files.exists(payloadDir)) {
      Files.walkFileTree(payloadDir, new PayloadFileExistsInAllManifestsVistor(payLoadManifests, ignoreHiddenFiles));
    }
  }
}
