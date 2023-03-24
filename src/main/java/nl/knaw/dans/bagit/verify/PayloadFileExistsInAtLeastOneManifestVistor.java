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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.Set;

import nl.knaw.dans.bagit.exceptions.FileNotInManifestException;
import org.slf4j.helpers.MessageFormatter;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class PayloadFileExistsInAtLeastOneManifestVistor extends AbstractPayloadFileExistsInManifestsVistor {
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private transient final Set<Path> filesListedInManifests;

  public PayloadFileExistsInAtLeastOneManifestVistor(final Set<Path> filesListedInManifests, final boolean ignoreHiddenFiles) {
    super(ignoreHiddenFiles);
    this.filesListedInManifests = filesListedInManifests;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException, FileNotInManifestException {
	if(Files.isHidden(path) && ignoreHiddenFiles){
	  logger.debug(messages.getString("skipping_hidden_file"), path);
  }
	else {
	  if(Files.isRegularFile(path) && !filesListedInManifests.contains(path.normalize())){
      final String formattedMessage = messages.getString("file_not_in_any_manifest_error");
      throw new FileNotInManifestException(MessageFormatter.format(formattedMessage, path).getMessage());
    }
    logger.debug(messages.getString("file_in_at_least_one_manifest"), path);
	}
	return FileVisitResult.CONTINUE;
  }

}
