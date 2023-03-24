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
package nl.knaw.dans.bagit.conformance;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of the BagIt conformance suite. 
 * This checker gives a warning if a file is not using UTF-8 encoding which is standard on most filesystems today.
 */
public interface EncodingChecker {
  Logger logger = LoggerFactory.getLogger(EncodingChecker.class);
  ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  /*
   * It is now normal for all files to be UTF-8
   */
  static void checkEncoding(final Charset encoding, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    if(!warningsToIgnore.contains(BagitWarning.TAG_FILES_ENCODING) && !StandardCharsets.UTF_8.equals(encoding)){
      logger.warn(messages.getString("tag_files_not_encoded_with_utf8_warning"), encoding);
      warnings.add(BagitWarning.TAG_FILES_ENCODING);
    }
  }
}
