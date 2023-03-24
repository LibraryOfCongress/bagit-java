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

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a metadata's value is not in the acceptable list of values
 */
public class MetatdataValueIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public MetatdataValueIsNotAcceptableException(final String message, final String metadataKey, final List<String> acceptableValues, final String actualValue) {
    super(MessageFormatter.arrayFormat(message, new Object[]{metadataKey, acceptableValues, actualValue}).getMessage());
  }
}
