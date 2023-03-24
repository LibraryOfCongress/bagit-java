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
package nl.knaw.dans.bagit.writer;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RelativePathWriterTest extends PrivateConstructorTest {
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(RelativePathWriter.class);
  }
  
  @Test
  public void testRelativePath(){
    Path parent = Paths.get("/foo");
    Path child = parent.resolve("bar/ham");
    String expectedRelativePath = "bar/ham" + System.lineSeparator();
    
    Assertions.assertEquals(expectedRelativePath, RelativePathWriter.formatRelativePathString(parent, child));
  }

  @Test
  public void testUsingBothRelativeAndAbsolutePaths(){
    Path parent = Paths.get("one/two");
    Path child = Paths.get("one/two/three").toAbsolutePath();
    String expectedRelativePath = "three" + System.lineSeparator();

    Assertions.assertEquals(expectedRelativePath, RelativePathWriter.formatRelativePathString(parent, child));
  }
}
