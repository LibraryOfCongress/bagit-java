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
package nl.knaw.dans.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Version;
import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;
import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;
import nl.knaw.dans.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import nl.knaw.dans.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;

/**
 * Responsible for reading a bag from the filesystem.
 */
public final class BagReader {
  private final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  
  public BagReader(){
    this.nameMapping = new StandardBagitAlgorithmNameToSupportedAlgorithmMapping();
  }
  
  public BagReader(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this.nameMapping = nameMapping;
  }
  
  /**
   * Read the bag from the filesystem and create a bag object
   * 
   * @param rootDir the root directory of the bag 
   * @throws IOException if there is a problem reading a file
   * @return a {@link Bag} object representing a bag on the filesystem
   * 
   * @throws UnparsableVersionException If there is a problem parsing the bagit version
   * @throws MaliciousPathException if there is path that is referenced in the manifest or fetch file that is outside the bag root directory
   * @throws InvalidBagMetadataException if the metadata or bagit.txt file does not conform to the bagit spec
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest or fetch file is not formatted properly
   */
  public Bag read(final Path rootDir) throws IOException, UnparsableVersionException, MaliciousPathException, InvalidBagMetadataException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    final Bag bag = new Bag();
    
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    bag.setRootDir(rootDir);
    
    final Path bagitFile = bagitDir.resolve("bagit.txt");
    final SimpleImmutableEntry<Version, Charset> bagitInfo = BagitTextFileReader.readBagitTextFile(bagitFile);
    bag.setVersion(bagitInfo.getKey());
    bag.setFileEncoding(bagitInfo.getValue());
    
    ManifestReader.readAllManifests(nameMapping, bagitDir, bag);
    
    bag.getMetadata().addAll(MetadataReader.readBagMetadata(bagitDir, bag.getFileEncoding()));
    
    final Path fetchFile = bagitDir.resolve("fetch.txt");
    if(Files.exists(fetchFile)){
      bag.getItemsToFetch().addAll(FetchReader.readFetch(fetchFile, bag.getFileEncoding(), bag.getRootDir()));
    }
    
    return bag;
  }
  
  public BagitAlgorithmNameToSupportedAlgorithmMapping getNameMapping() {
    return nameMapping;
  }
}
