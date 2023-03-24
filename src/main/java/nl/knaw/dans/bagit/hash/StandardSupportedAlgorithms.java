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

/**
 * The standard algorithms that are supported "out of the box" in bagit
 */
public enum StandardSupportedAlgorithms implements SupportedAlgorithm{
  MD5("MD5"),
  SHA1("SHA-1"),
  SHA224("SHA-224"),
  SHA256("SHA-256"),
  SHA512("SHA-512");

  private final String messageDigestName;
  
  private StandardSupportedAlgorithms(final String messageDigestName){
    this.messageDigestName = messageDigestName;
  }

  @Override
  public String getMessageDigestName() {
    return messageDigestName;
  }

  @SuppressWarnings({"PMD.UseLocaleWithCaseConversions"})
  @Override
  public String getBagitName() {
    return name().toLowerCase();
  }
}
