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
package nl.knaw.dans.bagit.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.Hasher;
import nl.knaw.dans.bagit.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * responsible for writing out a {@link Bag}
 */
public final class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(BagWriter.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

  private BagWriter(){
    //intentionally left empty
  }
  
  /**
   * Write the bag out to the specified directory. 
   * If an error occurs some of the files may have been written out to the filesystem.
   * tag manifest(s) are updated prior to writing to ensure bag is valid after completion, 
   * it is therefore recommended if you are going to further interact with the bag to read it from specified outputDir path
   * 
   * @param bag the {@link Bag} object to write out
   * @param outputDir the output directory that will become the root of the bag
   * 
   * @throws IOException if there is a problem writing a file
   * @throws NoSuchAlgorithmException when trying to generate a {@link MessageDigest} which is used during update.
   */
  public static void write(final Bag bag, final Path outputDir) throws IOException, NoSuchAlgorithmException{
    logger.debug(messages.getString("writing_payload_files"));
    final Path bagitDir = PayloadWriter.writeVersionDependentPayloadFiles(bag, outputDir);
    
    logger.debug(messages.getString("upsert_payload_oxum"));
    final String payloadOxum = PathUtils.generatePayloadOxum(PathUtils.getDataDir(bag.getVersion(), outputDir));
    bag.getMetadata().upsertPayloadOxum(payloadOxum);
    
    logger.debug(messages.getString("writing_bagit_file"));
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), bagitDir);
    
    logger.debug(messages.getString("writing_payload_manifests"));
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), bagitDir, bag.getRootDir(), bag.getFileEncoding());

    if(!bag.getMetadata().isEmpty()){
      logger.debug(messages.getString("writing_bag_metadata"));
      MetadataWriter.writeBagMetadata(bag.getMetadata(), bag.getVersion(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getItemsToFetch().size() > 0){
      logger.debug(messages.getString("writing_fetch_file"));
      FetchWriter.writeFetchFile(bag.getItemsToFetch(), bagitDir, bag.getRootDir(), bag.getFileEncoding());
    }
    if(bag.getTagManifests().size() > 0){
      logger.debug(messages.getString("writing_tag_manifests"));
      writeTagManifestFiles(bag.getTagManifests(), bagitDir, bag.getRootDir());
      final Set<Manifest> updatedTagManifests = updateTagManifests(bag, outputDir);
      bag.setTagManifests(updatedTagManifests);
      ManifestWriter.writeTagManifests(updatedTagManifests, bagitDir, outputDir, bag.getFileEncoding());
    }
  }
  
  /*
   * Update the tag manifest cause the checksum of the other tag files will have changed since we just wrote them out to disk
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static Set<Manifest> updateTagManifests(final Bag bag, final Path newBagRootDir) throws NoSuchAlgorithmException, IOException{
    final Set<Manifest> newManifests = new HashSet<>();
    
    for(final Manifest tagManifest : bag.getTagManifests()){
      final Manifest newManifest = new Manifest(tagManifest.getAlgorithm());
      
      for(final Path originalPath : tagManifest.getFileToChecksumMap().keySet()){
        final Path relativePath = bag.getRootDir().relativize(originalPath);
        final Path pathToUpdate = newBagRootDir.resolve(relativePath);
        final MessageDigest messageDigest = MessageDigest.getInstance(tagManifest.getAlgorithm().getMessageDigestName());
        final String newChecksum = Hasher.hash(pathToUpdate, messageDigest);
        newManifest.getFileToChecksumMap().put(pathToUpdate, newChecksum);
      }
      
      newManifests.add(newManifest);
    }
    
    return newManifests;
  }
  
  /*
   * Write the tag manifest files
   */
  private static void writeTagManifestFiles(final Set<Manifest> manifests, final Path outputDir, final Path bagRootDir) throws IOException{
    for(final Manifest manifest : manifests){
      for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
        final Path relativeLocation = bagRootDir.relativize(entry.getKey());
        final Path writeTo = outputDir.resolve(relativeLocation);
        final Path writeToParent = writeTo.getParent();
        if(!Files.exists(writeTo) && writeToParent != null){
          Files.createDirectories(writeToParent);
          Files.copy(entry.getKey(), writeTo);
        }
      }
    }
  }
  
  
}
