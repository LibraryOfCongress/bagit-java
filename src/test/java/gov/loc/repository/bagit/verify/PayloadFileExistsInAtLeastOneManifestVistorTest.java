package gov.loc.repository.bagit.verify;

import java.io.File;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.exceptions.FileNotInManifestException;

public class PayloadFileExistsInAtLeastOneManifestVistorTest extends Assert {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test(expected=FileNotInManifestException.class)
  public void testFileNotInManifestException() throws Exception{
    File file = folder.newFile();
    
    PayloadFileExistsInAtLeastOneManifestVistor sut = new PayloadFileExistsInAtLeastOneManifestVistor(new HashSet<>(), true);
    sut.visitFile(file.toPath(), null);
  }
}
