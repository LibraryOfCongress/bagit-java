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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;

public class HasherTest extends PrivateConstructorTest {

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(Hasher.class);
  }
  
  @Test
  public void testBasicHash() throws IOException, NoSuchAlgorithmException{
    Path path = Paths.get(new File("src/test/resources/bagitFiles/bagit-0.97.txt").toURI());
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    String expectedHash = "41b89090f32a9ef33226b48f1b98dddf";
    
    String hash = Hasher.hash(path, messageDigest);
    Assertions.assertEquals(expectedHash, hash);
  }
  
  @Test
  public void testHashBadInput() throws IOException, NoSuchAlgorithmException{
    Path path = createDirectory("newPath");
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Assertions.assertThrows(IOException.class, () -> { Hasher.updateMessageDigests(path, Arrays.asList(messageDigest)); });
  }
}
