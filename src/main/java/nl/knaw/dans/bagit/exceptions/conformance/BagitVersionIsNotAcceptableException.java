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
package nl.knaw.dans.bagit.exceptions.conformance;

import java.util.List;

import nl.knaw.dans.bagit.domain.Version;
import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when the bag's version is not in the acceptable list of versions
 */
public class BagitVersionIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public BagitVersionIsNotAcceptableException(final String message, final Version version, final List<String> acceptableVersions) {
    super(MessageFormatter.format(message, version, acceptableVersions).getMessage());
  }
}
