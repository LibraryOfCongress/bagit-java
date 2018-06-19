package gov.loc.repository.bagit.conformance;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.reader.BagReader;

public class BagLinterTest extends PrivateConstructorTest{
  
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagLinter.class);
  }
  
  @Test
  public void testLintBag() throws Exception{
    Set<BagitWarning> expectedWarnings = new HashSet<>();
    expectedWarnings.addAll(Arrays.asList(BagitWarning.values()));
    expectedWarnings.remove(BagitWarning.MANIFEST_SETS_DIFFER); //only applies to version 1.0 but need older version for other warnings, so we test this separately
    Set<BagitWarning> warnings = BagLinter.lintBag(rootDir);

    if(FileSystems.getDefault().getClass().getName() == "sun.nio.fs.MacOSXFileSystem"){
      expectedWarnings.remove(BagitWarning.DIFFERENT_NORMALIZATION); //don't test normalization on mac
    }
    
    Set<BagitWarning> diff = new HashSet<>(expectedWarnings);
    diff.removeAll(warnings);
    
    Assertions.assertEquals(expectedWarnings, warnings, "Warnings missing: " + diff.toString() + "\n");
  }
  
  @Test
  public void testCheckAgainstProfile() throws Exception{
    Path profileJson = new File("src/test/resources/bagitProfiles/exampleProfile.json").toPath();
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/profileConformantBag").toPath();
    BagReader reader = new BagReader();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagLinter.checkAgainstProfile(inputStream, bag);
    }
  }
  
  @Test
  public void testIgnoreCheckForExtraLines() throws Exception{
    Set<BagitWarning> warnings = BagLinter.lintBag(rootDir, Arrays.asList(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES));
    Assertions.assertFalse(warnings.contains(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES));
  }
}