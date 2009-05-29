package gov.loc.repository.bagit.transfer;

import static junit.framework.Assert.*;
import org.junit.Test;

public class ThresholdFailStrategyTest
{
	private ThresholdFailStrategy unit = new ThresholdFailStrategy();
	
	@Test
	public void testFailsGlobally()
	{
		this.unit.setFileFailureThreshold(2);
		this.unit.setTotalFailureThreshold(3);
		
		this.check("uri-1", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-2", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-3", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-4", FetchFailureAction.STOP);
		this.check("uri-1", FetchFailureAction.STOP);
		this.check("uri-2", FetchFailureAction.STOP);
		this.check("uri-3", FetchFailureAction.STOP);
	}
	
	@Test
	public void testFailsForFile()
	{
		this.unit.setFileFailureThreshold(2);
		this.unit.setTotalFailureThreshold(10);
		
		this.check("uri-1", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-2", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-1", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-2", FetchFailureAction.RETRY_CURRENT);
		this.check("uri-1", FetchFailureAction.CONTINUE_WITH_NEXT);
		this.check("uri-2", FetchFailureAction.CONTINUE_WITH_NEXT);
		this.check("uri-1", FetchFailureAction.CONTINUE_WITH_NEXT);
		this.check("uri-2", FetchFailureAction.CONTINUE_WITH_NEXT);
	}
	
	private void check(String uri, FetchFailureAction expectedAction)
	{
		assertEquals(expectedAction, this.unit.registerFailure(uri, null, null));
	}
}
