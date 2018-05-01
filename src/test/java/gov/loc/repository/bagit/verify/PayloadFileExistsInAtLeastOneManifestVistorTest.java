package gov.loc.repository.bagit.verify;

import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.TempFolderTest;
import gov.loc.repository.bagit.exceptions.FileNotInManifestException;

public class PayloadFileExistsInAtLeastOneManifestVistorTest extends TempFolderTest {

  @Test
  public void testFileNotInManifestException() throws Exception{
    
    PayloadFileExistsInAtLeastOneManifestVistor sut = new PayloadFileExistsInAtLeastOneManifestVistor(new HashSet<>(), true);
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.visitFile(createFile("aNewFile"), null); });
  }
}
