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

import org.slf4j.helpers.MessageFormatter;

/**
 * If the version string in the bagit.txt file was not in the form &lt;MAJOR&gt;.&lt;MINOR&gt; 
 */
public class UnparsableVersionException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnparsableVersionException(final String message, final String version){
    super(MessageFormatter.format(message, version).getMessage());
  }
}
