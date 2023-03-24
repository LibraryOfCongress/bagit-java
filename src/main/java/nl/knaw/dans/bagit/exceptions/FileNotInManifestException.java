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
package nl.knaw.dans.bagit.exceptions;

import java.io.IOException;

/**
 * Class to represent an error when a file is found in the payload directory but not in any manifest.
 * Opposite to {@link FileNotInPayloadDirectoryException}
 */
public class FileNotInManifestException extends IOException {
  private static final long serialVersionUID = 1L;

  public FileNotInManifestException(final String message){
    super(message);
  }
}
