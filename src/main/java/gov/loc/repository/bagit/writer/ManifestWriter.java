package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;
import java.io.BufferedWriter;

/**
 * Responsible for writing out a {@link Manifest} to the filesystem
 */
public final class ManifestWriter{
  private static final Logger logger = LoggerFactory.getLogger(PayloadWriter.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private ManifestWriter(){
    //intentionally left empty
  }
  
  /**
   * Write the payload <b>manifest(s)</b> to the output directory
   * 
   * @param manifests the payload{@link Manifest}s to write out
   * @param outputDir the root of where the manifest is being written to
   * @param bagitRootDir the path to the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadManifests(final Set<Manifest> manifests, final Path outputDir, final Path bagitRootDir, final Charset charsetName) throws IOException{
    logger.info(messages.getString("writing_payload_manifests"));
    writeManifests(manifests, outputDir, bagitRootDir, "manifest-", charsetName);
  }
  
  /**
   * Write the tag <b>manifest(s)</b> to the output directory
   * 
   * @param tagManifests the tag{@link Manifest}s to write out
   * @param outputDir the root of where the manifest is being written to
   * @param bagitRootDir the path to the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeTagManifests(final Set<Manifest> tagManifests, final Path outputDir, final Path bagitRootDir, final Charset charsetName) throws IOException{
    logger.info(messages.getString("writing_tag_manifests"));
    writeManifests(tagManifests, outputDir, bagitRootDir, "tagmanifest-", charsetName);
  }
  
  /*
   * Generic method to write manifests
   */
  private static void writeManifests(final Set<Manifest> manifests, final Path outputDir, final Path relativeTo, final String filenameBase, final Charset charsetName) throws IOException{
    for(final Manifest manifest : manifests){
      final Path manifestPath = outputDir.resolve(filenameBase + manifest.getAlgorithm().getBagitName() + ".txt");
      logger.debug(messages.getString("writing_manifest_to_path"), manifestPath);

      Files.deleteIfExists(manifestPath);
      Files.createFile(manifestPath);

      try (BufferedWriter writer = Files.newBufferedWriter(manifestPath, charsetName,
            StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
        for (final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()) {
          //there are 2 spaces between the checksum and the path so that the manifests are compatible with the md5sum tools available on most unix systems.
          //This may cause problems on windows due to it being text mode, in which case either replace with a * or try verifying in binary mode with --binary
          final String line = entry.getValue() + "  " + RelativePathWriter.formatRelativePathString(relativeTo, entry.getKey());
          logger.debug(messages.getString("writing_line_to_file"), line, manifestPath);
          writer.append(line);
        }
      }
    }
  }
}
