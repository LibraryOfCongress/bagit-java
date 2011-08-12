package gov.loc.repository.bagit.progresslistener;

import gov.loc.repository.bagit.ProgressListener;

public class PrintingProgressListener implements ProgressListener
{
	@Override
	public void reportProgress(String activity, Object item, Long count, Long total) 
	{
		System.out.println(ProgressListenerHelper.format(activity, item, count, total));
	}
}
