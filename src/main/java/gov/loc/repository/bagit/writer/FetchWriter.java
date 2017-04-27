package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.FetchItem;

/**
 * Responsible for writing out the list of {@link FetchItem} to the fetch.txt file on the filesystem
 */
public final class FetchWriter {
  private static final Logger logger = LoggerFactory.getLogger(FetchWriter.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private FetchWriter(){
    //intentionally left empty
  }
  
  /**
   * Write the fetch.txt file to the outputDir with the specified encoding (charsetName)
   * 
   * @param itemsToFetch the list of {@link FetchItem}s to write into the fetch.txt
   * @param outputDir the root of the bag
   * @param bagitRootDir the path to the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeFetchFile(final List<FetchItem> itemsToFetch, final Path outputDir, final Path bagitRootDir, final Charset charsetName) throws IOException{
    logger.debug(messages.getString("writing_fetch_file_to_path"), outputDir);
    final Path fetchFilePath = outputDir.resolve("fetch.txt");
    
    for(final FetchItem item : itemsToFetch){
      final String line = formatFetchLine(item, bagitRootDir);
      logger.debug(messages.getString("writing_line_to_file"), line, fetchFilePath);
      Files.write(fetchFilePath, line.getBytes(charsetName), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  private static String formatFetchLine(final FetchItem fetchItem, final Path bagitRootDir){
    final StringBuilder sb = new StringBuilder();
    sb.append(fetchItem.getUrl()).append(' ');
    
    if(fetchItem.getLength() == null || fetchItem.getLength() < 0){
      sb.append("- ");
    }
    else{
      sb.append(fetchItem.getLength()).append(' ');
    }
    
    sb.append(RelativePathWriter.formatRelativePathString(bagitRootDir, fetchItem.getPath()));
      
    return sb.toString();
  }
}
