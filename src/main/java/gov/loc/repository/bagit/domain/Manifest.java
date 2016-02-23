
package gov.loc.repository.bagit.domain;

import java.io.File;
import java.util.HashMap;

/**
 * A manifest is a list of files and their corresponding checksum with the algorithm used to generate that checksum
 */
public class Manifest {
  private final String algorithm;
  private HashMap<File, String> fileToChecksumMap = new HashMap<>();
  
  public Manifest(String algorithm){
    this.algorithm = algorithm;
  }

  public HashMap<File, String> getFileToChecksumMap() {
    return fileToChecksumMap;
  }

  public void setFileToChecksumMap(HashMap<File, String> fileToChecksumMap) {
    this.fileToChecksumMap = fileToChecksumMap;
  }

  public String getAlgorithm() {
    return algorithm;
  }
}
