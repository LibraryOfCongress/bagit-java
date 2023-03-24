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

import java.nio.file.Path;

import org.slf4j.helpers.MessageFormatter;

/**
 * The payload directory is a required file. This class represents the error if it is not found.
 */
public class MissingPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadDirectoryException(final String message, final Path path){
    super(MessageFormatter.format(message, path).getMessage());
  }
}
