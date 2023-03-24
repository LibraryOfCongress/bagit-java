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
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;

import nl.knaw.dans.bagit.domain.Manifest;

/**
 * Creates the tag manifests by walking the tag files and calculating their checksums
 * Mainly used in {@link BagCreator}
 */
public class CreateTagManifestsVistor extends AbstractCreateManifestsVistor{
  
  public CreateTagManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    super(manifestToMessageDigestMap, includeHiddenFiles);
  }
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    return abstractPreVisitDirectory(dir, "data");
  }
}
