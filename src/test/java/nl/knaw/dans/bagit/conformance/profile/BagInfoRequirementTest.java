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
package nl.knaw.dans.bagit.conformance.profile;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BagInfoRequirementTest {

  @Test
  public void testEquals(){
    BagInfoRequirement requirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    BagInfoRequirement sameRequirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    Assertions.assertEquals(requirement, sameRequirement);
    
    Assertions. assertFalse(requirement.equals(null));
    
    BagInfoRequirement differentRequirement = new BagInfoRequirement(false, Arrays.asList("foo"));
    Assertions.assertFalse(requirement.equals(differentRequirement));
    
    BagInfoRequirement differentListOfAcceptableValues = new BagInfoRequirement();
    differentListOfAcceptableValues.setRequired(true);
    differentListOfAcceptableValues.setAcceptableValues(Arrays.asList("bar"));
    differentListOfAcceptableValues.setRepeatable(false);
    Assertions.assertFalse(requirement.equals(differentListOfAcceptableValues));
  }
}
