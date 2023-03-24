/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import nl.knaw.dans.bagit.hash.SupportedAlgorithm;

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
