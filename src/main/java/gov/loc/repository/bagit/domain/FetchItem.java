package gov.loc.repository.bagit.domain;

import java.net.URL;

/**
 * An individual item to fetch as specified by 
 * <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-2.2.3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-2.2.3</a>
 */
public class FetchItem {
  /**
   * The url from which the item can be downloaded
   */
  public final URL url;
  
  /**
   * The length of the file in octets
   */
  public final Long length; 
  
  /**
   * The path relative to the /data directory
   */
  public final String path;
  
  public FetchItem(URL url, Long length, String path){
    this.url = url;
    this.length = length;
    this.path = path;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(url).append(' ');
    
    if(length < 0){
      sb.append('-').append(' ');
    }
    else{
      sb.append(length).append(' ');
    }
    
    sb.append(path).append(System.lineSeparator());
      
    return sb.toString();
  }
}
