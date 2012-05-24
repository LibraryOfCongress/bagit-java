package gov.loc.repository.bagit.verify.impl;

import static org.junit.Assert.*;

import java.io.File;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import org.junit.Test;

public class PayloadOxumVerifierTest {
    
	PayloadOxumVerifier verifier = new PayloadOxumVerifier();
    
    BagFactory bagFactory = new BagFactory();
    
    
    @Test
    public void testVerifyPayloadOxum() throws Exception {
    	Bag testBag = this.getBag();
    	try {
    		//No payload-oxum return true
    		SimpleResult result = verifier.verify(testBag);
    		assertTrue(result.isSuccess());
    		assertEquals(1, result.getWarningMessages().size());
    		
    		//Correct payload-oxum
    		testBag.getBagInfoTxt().setPayloadOxum("25.5");
    		result = verifier.verify(testBag);
    		assertTrue(result.isSuccess());
    		
    		//Incorrect payload-oxum
    		testBag.getBagInfoTxt().setPayloadOxum("25.4");
    		result = verifier.verify(testBag);
    		assertFalse(result.isSuccess());
    		assertTrue(result.hasSimpleMessage(PayloadOxumVerifier.CODE_INCORRECT_PAYLOAD_OXUM));
    	} finally {
    		testBag.close();
    	}
    	
    }
    
    private Bag getBag() throws Exception {
        File bagDir = ResourceHelper.getFile("bags/v0_96/bag"); 
    	return this.bagFactory.createBag(bagDir);  
    }   
    
}
