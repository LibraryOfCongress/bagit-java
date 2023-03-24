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

import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.bagit.domain.Version;

/**
 * Part of the BagIt conformance suite. 
 * This checker gives a warning if a bag is not using the latest bagit version
 */
public interface VersionChecker {
  Logger logger = LoggerFactory.getLogger(VersionChecker.class);
  Version LATEST_BAGIT_VERSION = Version.LATEST_BAGIT_VERSION();
  ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  /*
   * Check that they are using the latest version
   */
  static void checkVersion(final Version version, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    if(!warningsToIgnore.contains(BagitWarning.OLD_BAGIT_VERSION) && version.isOlder(LATEST_BAGIT_VERSION)){
      logger.warn(messages.getString("old_version_warning"), version, LATEST_BAGIT_VERSION);
      warnings.add(BagitWarning.OLD_BAGIT_VERSION);
    }
  }
}
