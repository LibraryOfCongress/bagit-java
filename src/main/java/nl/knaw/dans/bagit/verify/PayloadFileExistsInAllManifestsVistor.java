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
package nl.knaw.dans.bagit.verify;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.exceptions.FileNotInManifestException;
import org.slf4j.helpers.MessageFormatter;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class PayloadFileExistsInAllManifestsVistor extends AbstractPayloadFileExistsInManifestsVistor {
  private transient final Set<Manifest> manifests;

  public PayloadFileExistsInAllManifestsVistor(final Set<Manifest> manifests, final boolean ignoreHiddenFiles) {
    super(ignoreHiddenFiles);
    this.manifests = manifests;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws FileNotInManifestException {
    if(Files.isRegularFile(path)){
      for(final Manifest manifest : manifests){
        if(!manifest.getFileToChecksumMap().keySet().contains(path.normalize())){
          final String formattedMessage = messages.getString("file_not_in_manifest_error");
          throw new FileNotInManifestException(MessageFormatter.format(formattedMessage, path, manifest.getAlgorithm().getBagitName()).getMessage());
        }
      }
    }
    logger.debug(messages.getString("file_in_all_manifests"), path);
    return FileVisitResult.CONTINUE;
  }
}
