package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.util.PathUtils;

public final class ManifestReader {
  private static final Logger logger = LoggerFactory.getLogger(ManifestReader.class);
  
  private ManifestReader(){
    //intentionally left empty
  }
  
  /**
   * Finds and reads all manifest files in the rootDir and adds them to the given bag.
   * 
   * @param rootDir the directory that contain the manifest(s)
   * @param bag to update with the manifests
   * 
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest is not formatted properly
   */
  static void readAllManifests(final Path rootDir, final Bag bag) throws IOException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    logger.info("Attempting to find and read manifests");
    final DirectoryStream<Path> manifests = getAllManifestFiles(rootDir);
    
    for (final Path path : manifests){
      final String filename = PathUtils.getFilename(path);
      
      if(filename.startsWith("tagmanifest-")){
        logger.debug("Found tag manifest [{}]", path);
        bag.getTagManifests().add(readManifest(path, bag.getRootDir(), bag.getFileEncoding()));
      }
      else if(filename.startsWith("manifest-")){
        logger.debug("Found payload manifest [{}]", path);
        bag.getPayLoadManifests().add(readManifest(path, bag.getRootDir(), bag.getFileEncoding()));
      }
    }
  }
  
  /*
   * Get a list of all the tag and payload manifests
   */
  private static DirectoryStream<Path> getAllManifestFiles(final Path rootDir) throws IOException{
    final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
      public boolean accept(final Path file) throws IOException {
        if(file == null || file.getFileName() == null){ return false;}
        final String filename = PathUtils.getFilename(file);
        return filename.startsWith("tagmanifest-") || filename.startsWith("manifest-");
      }
    };
    
    return Files.newDirectoryStream(rootDir, filter);
  }
  
  /**
   * Reads a manifest file and converts it to a {@link Manifest} object.
   * 
   * @param manifestFile a specific manifest file
   * @param bagRootDir the root directory of the bag
   * @param charset the encoding to use when reading the manifest file
   * @return the converted manifest object from the file
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest is not formatted properly
   */
  public static Manifest readManifest(final Path manifestFile, final Path bagRootDir, final Charset charset) 
          throws IOException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    logger.debug("Reading manifest [{}]", manifestFile);
    final String alg = PathUtils.getFilename(manifestFile).split("[-\\.]")[1];
    
    final Manifest manifest = new Manifest(alg);
    
    final Map<Path, String> filetToChecksumMap = readChecksumFileMap(manifestFile, bagRootDir, charset);
    manifest.setFileToChecksumMap(filetToChecksumMap);
    
    return manifest;
  }
  
  /*
   * read the manifest file into a map of files and checksums
   */
  static Map<Path, String> readChecksumFileMap(final Path manifestFile, final Path bagRootDir, final Charset charset) throws IOException, MaliciousPathException, InvalidBagitFileFormatException{
    final HashMap<Path, String> map = new HashMap<>();
    final BufferedReader br = Files.newBufferedReader(manifestFile, charset);

    String line = br.readLine();
    while(line != null){
      final String[] parts = line.split("\\s+", 2);
      final Path file = TagFileReader.createFileFromManifest(bagRootDir, parts[1]);
      logger.debug("Read checksum [{}] and file [{}] from manifest [{}]", parts[0], file, manifestFile);
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    br.close();
    
    return map;
  }
}
