package gov.loc.repository.bagit.progresslistener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.ProgressListener;

public class LoggingProgressListener implements ProgressListener 
{
    private Log log;
    
    public LoggingProgressListener(String logName)
    {
    	this.log = LogFactory.getLog(logName);
    }
    
    public LoggingProgressListener(Class<?> clazz)
    {
    	this.log = LogFactory.getLog(clazz);
    }
    
    public LoggingProgressListener()
    {
    	this(LoggingProgressListener.class);
    }

	@Override
	public void reportProgress(String activity, Object item, Long count, Long total)
	{
		log.info(ProgressListenerHelper.format(activity, item, count, total));
	}
}
