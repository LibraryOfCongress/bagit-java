package gov.loc.repository.bagit.v0_96;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.AbstractBagImplTest;

public class BagImplTest extends AbstractBagImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_96;
	}
	
	@Override
	public void performTestBagWithTagDirectory(Bag bag) {
		performTestBagWithTagDirectoryPrev97(bag);		
	}

	@Override
	public void performTestBagWithIgnoredTagDirectory(Bag bag) {
		performTestBagWithIgnoredTagDirectoryPrev97(bag);		
	}

  @Override
  public String getValidZipBag() {
    return "bags/v0_96/bag.zip";
  }
	
  @Override
  public String getInvalidZipBag() {
    return "bags/v0_96/invalidBag.zip";
  }
  
  @Override
  public String getValidBagFolder() {
    return "bags/v0_96/bag";
  }

  @Override
  public String getInvalidBagFolder() {
    return "bags/v0_96/invalidBag";
  }
}