package gov.loc.repository.bagit.conformance;

import org.junit.Assert;
import org.junit.Test;

public class ManifestCheckerTest extends Assert{

  @Test
  public void testOSSpecificFilesRegex(){
    String regex = ManifestChecker.getOsFilesRegex();
    String[] osFilesToTest = new String[]{"data/Thumbs.db", "data/.DS_Store", "data/.Spotlight-V100", "data/.Trashes", 
        "data/._.Trashes", "data/.fseventsd"};
    
    for(String osFileToTest : osFilesToTest){
      assertTrue(osFileToTest + " should match regex but it doesn't", osFileToTest.matches(regex));
    }
  }
}
