
package gov.loc.repository.bagit.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gov.loc.repository.bagit.hash.SupportedAlgorithm;

/**
 * A manifest is a list of files and their corresponding checksum with the algorithm used to generate that checksum
 */
public final class Manifest {
  private final SupportedAlgorithm algorithm;
  private Map<Path, String> fileToChecksumMap = new HashMap<>();
  
  public Manifest(final SupportedAlgorithm algorithm){
    this.algorithm = algorithm;
  }

  public Map<Path, String> getFileToChecksumMap() {
    return fileToChecksumMap;
  }

  public void setFileToChecksumMap(final Map<Path, String> fileToChecksumMap) {
    this.fileToChecksumMap = fileToChecksumMap;
  }

  public SupportedAlgorithm getAlgorithm() {
    return algorithm;
  }

  @Override
  public String toString() {
    return "Manifest [algorithm=" + algorithm + ", fileToChecksumMap=" + fileToChecksumMap + "]";
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(algorithm) + fileToChecksumMap.hashCode();
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
    
    return Objects.equals(algorithm, other.algorithm) && fileToChecksumMap.equals(other.getFileToChecksumMap()); 
  }
}
