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
package nl.knaw.dans.bagit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Assertions;

/**
 * Used for testing classes that have private constructors so that code coverage
 * is better
 */
abstract public class PrivateConstructorTest extends TempFolderTest{

  /*
   * http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor/10872497#10872497 
   */
  public static void assertUtilityClassWellDefined(final Class<?> clazz)
      throws NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {
    Assertions.assertTrue(Modifier.isFinal(clazz.getModifiers()), "class must be final");
    Assertions.assertEquals(1, clazz.getDeclaredConstructors().length, "There must be only one constructor");
    final Constructor<?> constructor = clazz.getDeclaredConstructor();
    if (constructor.isAccessible() ||
        !Modifier.isPrivate(constructor.getModifiers())) {
      Assertions.fail("constructor is not private");
    }
    constructor.setAccessible(true);
    constructor.newInstance();
    constructor.setAccessible(false);
    for (final Method method : clazz.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())
          && method.getDeclaringClass().equals(clazz)) {
        Assertions.fail("there exists a non-static method:" + method);
      }
    }
  }
}
