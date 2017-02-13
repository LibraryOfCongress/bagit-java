package gov.loc.repository.bagit.conformance;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Bag;

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
  
  @Test
  public void testCheckAgainstProfile() throws Exception{
    Path profileJson = new File("src/test/resources/bagitProfiles/exampleProfile.json").toPath();
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/profileConformantBag").toPath();
    Bag bag = sut.getReader().read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      sut.checkAgainstProfile(inputStream, bag);
    }
  }
}