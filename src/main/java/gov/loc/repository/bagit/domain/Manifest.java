
package gov.loc.repository.bagit.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A manifest is a list of files and their corresponding checksum with the algorithm used to generate that checksum
 */
public final class Manifest {
  private final String bagitAlgorithmName;
  private Map<Path, String> fileToChecksumMap = new HashMap<>();
  
  public Manifest(final String bagitAlgorithmName){
    this.bagitAlgorithmName = bagitAlgorithmName;
  }

  public Map<Path, String> getFileToChecksumMap() {
    return fileToChecksumMap;
  }

  public void setFileToChecksumMap(final Map<Path, String> fileToChecksumMap) {
    this.fileToChecksumMap = fileToChecksumMap;
  }

  public String getBagitAlgorithmName() {
    return bagitAlgorithmName;
  }

  @Override
  public String toString() {
    return "Manifest [bagitAlgorithmName=" + bagitAlgorithmName + ", fileToChecksumMap=" + fileToChecksumMap + "]";
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(bagitAlgorithmName) + fileToChecksumMap.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof Manifest)){
      return false;
    }
    
    final Manifest other = (Manifest) obj;
    
    return Objects.equals(bagitAlgorithmName, other.bagitAlgorithmName) && fileToChecksumMap.equals(other.getFileToChecksumMap()); 
  }
}
