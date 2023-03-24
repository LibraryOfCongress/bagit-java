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
package nl.knaw.dans.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;
import java.util.ResourceBundle;

import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.Hasher;
import nl.knaw.dans.bagit.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link SimpleFileVisitor} class that optionally avoids hidden files.
 * Mainly used in {@link BagCreator}
 */
public abstract class AbstractCreateManifestsVistor extends SimpleFileVisitor<Path>{
  private static final Logger logger = LoggerFactory.getLogger(AbstractCreateManifestsVistor.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  protected transient final Map<Manifest, MessageDigest> manifestToMessageDigestMap;
  protected transient final boolean includeHiddenFiles;
  
  public AbstractCreateManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    this.manifestToMessageDigestMap = manifestToMessageDigestMap;
    this.includeHiddenFiles = includeHiddenFiles;
  }
  
  public FileVisitResult abstractPreVisitDirectory(final Path dir, final String directoryToIgnore) throws IOException {
    if(!includeHiddenFiles && PathUtils.isHidden(dir) && !dir.endsWith(Paths.get(".bagit"))){
      logger.debug(messages.getString("skipping_hidden_file"), dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    if(dir.endsWith(directoryToIgnore)){ 
      logger.debug(messages.getString("skipping_ignored_directory"), dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException{
    if(!includeHiddenFiles && PathUtils.isHidden(path) && !path.endsWith(".keep")){
      logger.debug(messages.getString("skipping_hidden_file"), path);
    }
    else{
      Hasher.hash(path, manifestToMessageDigestMap);
    }
    
    return FileVisitResult.CONTINUE;
  }
}
