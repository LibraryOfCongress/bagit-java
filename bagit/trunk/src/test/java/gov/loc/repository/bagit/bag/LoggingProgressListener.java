package gov.loc.repository.bagit.bag;

import static java.text.MessageFormat.*;

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
		String msg;
		
		if (count != null)
		{
			if (total != null)
			{
				msg = format("{0} {1} ({2} of {3})", activity, item, count, total);
			}
			else
			{
				msg = format("{0} {1} ({2})", activity, item, count);
			}
		}
		else
		{
			msg = format("{0} {1}", activity, item);
		}

		log.debug(msg);
	}
}
