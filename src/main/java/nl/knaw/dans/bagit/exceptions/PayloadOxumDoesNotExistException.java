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

import nl.knaw.dans.bagit.domain.Bag;

/**
 * The {@link Bag} object should contain the Payload-Oxum metatdata key value pair,
 * this class represents the error when trying to calculate the payload-oxum and it doesn't exist on the bag object.
 */
public class PayloadOxumDoesNotExistException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PayloadOxumDoesNotExistException(final String message){
    super(message);
  }
}
