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
package nl.knaw.dans.bagit.hash;

import java.util.Locale;
import java.util.ResourceBundle;

import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;

/**
 * Provides a mapping between bagit algorithm names and {@link SupportedAlgorithm} 
 */
public class StandardBagitAlgorithmNameToSupportedAlgorithmMapping
    implements BagitAlgorithmNameToSupportedAlgorithmMapping {
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

  @Override
  public SupportedAlgorithm getSupportedAlgorithm(final String bagitAlgorithmName) throws UnsupportedAlgorithmException {
    try{
      return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase(Locale.getDefault()));
    }
    catch(IllegalArgumentException e){
      throw new UnsupportedAlgorithmException(messages.getString("algorithm_not_supported_error"), bagitAlgorithmName, e);
    }
  }
}
