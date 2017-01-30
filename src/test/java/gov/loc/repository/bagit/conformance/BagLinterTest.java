package gov.loc.repository.bagit.conformance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class BagLinterTest extends Assert{
  
  private BagLinter sut = new BagLinter();
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testLintBag() throws Exception{
    Set<BagitWarning> expectedWarnings = new HashSet<>();
    expectedWarnings.addAll(Arrays.asList(BagitWarning.values()));
    Set<BagitWarning> warnings = sut.lintBag(rootDir);

    if(System.getProperty("os.name").equals("Mac OS X")){
      expectedWarnings.remove(BagitWarning.DIFFERENT_NORMALIZATION); //don't test normalization on mac
    }
    
    Set<BagitWarning> diff = new HashSet<>(expectedWarnings);
    diff.removeAll(warnings);
    
    assertEquals("Warnings missing: " + diff.toString() + "\n", expectedWarnings, warnings);
  }
}