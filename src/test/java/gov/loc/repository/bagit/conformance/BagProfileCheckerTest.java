package gov.loc.repository.bagit.conformance;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;

public class BagProfileCheckerTest extends PrivateConstructorTest {
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagProfileChecker.class);
  }
}
