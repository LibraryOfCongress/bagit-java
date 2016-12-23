package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;

public final class FetchReader {
  private static final Logger logger = LoggerFactory.getLogger(FetchReader.class);

  private FetchReader(){
    //intentionally left empty
  }
  
  /**
   * Reads a fetch.txt file
   * 
   * @param fetchFile the specific fetch file
   * @param encoding the encoding to read the file with
   * @param bagRootDir the root directory of the bag
   * @return a list of items to fetch
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException if the path was crafted to point outside the bag directory
   * @throws InvalidBagitFileFormatException if the fetch format does not follow the bagit specification
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static List<FetchItem> readFetch(final Path fetchFile, final Charset encoding, final Path bagRootDir) throws IOException, MaliciousPathException, InvalidBagitFileFormatException{
    logger.info("Attempting to read [{}]", fetchFile);
    final BufferedReader br = Files.newBufferedReader(fetchFile, encoding);
    final List<FetchItem> itemsToFetch = new ArrayList<>();

    String line = br.readLine();
    String[] parts = null;
    long length = 0;
    URL url = null;
    while(line != null){
      parts = line.split("\\s+", 3);
      final Path path = TagFileReader.createFileFromManifest(bagRootDir, parts[2]);
      length = parts[1].equals("-") ? -1 : Long.decode(parts[1]);
      url = new URL(parts[0]);
      
      logger.debug("Read URL [{}] length [{}] path [{}] from fetch file [{}]", url, length, parts[2], fetchFile);
      final FetchItem itemToFetch = new FetchItem(url, length, path);
      itemsToFetch.add(itemToFetch);
      
      line = br.readLine();
    }

    return itemsToFetch;
  }
}
