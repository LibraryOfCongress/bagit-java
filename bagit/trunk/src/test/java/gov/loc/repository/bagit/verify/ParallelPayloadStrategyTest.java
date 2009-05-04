package gov.loc.repository.bagit.verify;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

import org.junit.Before;
import org.junit.Test;

public class ParallelPayloadStrategyTest 
{
    private ParallelPayloadStrategy unit = new ParallelPayloadStrategy();
    
    @Before
    public void setUp()
    {
        this.unit.setNumebrOfThreads(3);
    }
    
    @Test
    public void testCannotSetNumberOfThreadsToZero()
    {
        try
        {
            this.unit.setNumebrOfThreads(0);
            fail("Expected IllegalArgumentException was not thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
	@Test
	public void testVerifyCorrect() throws Exception
	{
	    Bag testBag = this.getBag(Version.V0_96, Format.FILESYSTEM);
	    SimpleResult result = testBag.checkAdditionalVerify(this.unit);
	    assertEquals(true, result.isSuccess());
	}

    private Bag getBag(Version version, Bag.Format format) throws Exception
    {
        return BagFactory.createBag(this.getBagDir(version, format), version);  
    }   
    
    private File getBagDir(Version version, Bag.Format format) throws Exception 
    {
        return ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag{1}", version.toString().toLowerCase(), format.extension));     
    }
}
