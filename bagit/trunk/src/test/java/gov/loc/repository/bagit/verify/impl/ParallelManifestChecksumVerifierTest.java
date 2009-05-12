package gov.loc.repository.bagit.verify.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.bag.PrintingProgressListener;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;

import org.junit.Before;
import org.junit.Test;

public class ParallelManifestChecksumVerifierTest 
{
    private ParallelManifestChecksumVerifier unit = new ParallelManifestChecksumVerifier();
    
    BagFactory bagFactory = new BagFactory();
    
    @Before
    public void setUp()
    {
        this.unit.setNumberOfThreads(3);
    }
    
    @Test
    public void testCannotSetNumberOfThreadsToZero()
    {
        try
        {
            this.unit.setNumberOfThreads(0);
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
	    this.unit.setProgressListener(new PrintingProgressListener());
	    SimpleResult result = this.unit.verify(testBag.getPayloadManifests(), testBag);
	    assertEquals(true, result.isSuccess());
	}

    private Bag getBag(Version version, Bag.Format format) throws Exception
    {
        return this.bagFactory.createBag(this.getBagDir(version, format), version, true);  
    }   
    
    private File getBagDir(Version version, Bag.Format format) throws Exception 
    {
        return ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag{1}", version.toString().toLowerCase(), format.extension));     
    }
}
