package gov.loc.repository.bagit.transfer;

import static java.text.MessageFormat.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link FetchFailStrategy} that fails in different ways
 * for different thresholds.
 * 
 * <p>This strategy has two different thresholds:
 * <ul>
 * 	<li>{@link #getTotalFailureThreshold() total failures}</li>
 * 	<li>{@link #getFileFailureThreshold() failures per-file}</li>
 * </ul></p>
 * 
 * <p>If a particular file (uniquely identified by the URI) fails,
 * the strategy will return
 * {@link FetchFailureAction#RETRY_CURRENT RETRY_CURRENT} until the
 * number of failures for that file exceeds the per-file threshold.
 * After meeting or exceeding that threshold, the strategy will return
 * {@link FetchFailureAction#CONTINUE_WITH_NEXT CONTINUE_WITH_NEXT}.</p> 
 * 
 * <p>If at any time the total number of failures (across
 * all files) meets or exceeds the total failures threshold, the
 * strategy will return {@link FetchFailureAction#STOP STOP}.</p>
 * 
 * <p>For both thresholds, a threshold of 0 will be exceeded immediately.</p>
 * 
 * <h3>Note</h3>
 * <p>For implementation simplicity, the maximum permitted
 * thresholds are {@link Integer#MAX_VALUE MAX_INT} - 1.</p> 
 * 
 * @version $Id$
 * @see FetchFailureAction
 * @see #getFileFailureThreshold()
 * @see #setFileFailureThreshold(int)
 * @see #getTotalFailureThreshold()
 * @see #setTotalFailureThreshold(int)
 */
public class ThresholdFailStrategy implements FetchFailStrategy
{
	private int totalFailureThreshold;
	private int fileFailureThreshold;
	private int totalFailures = 0;
	private Map<String, Integer> failures = new HashMap<String, Integer>();
	
	public int getTotalFailureThreshold() 
	{
		return totalFailureThreshold;
	}

	public void setTotalFailureThreshold(int totalFailureThreshold) 
	{
		if (totalFailureThreshold < 0)
		{
			throw new IllegalArgumentException(format("TotalFailureThreshold cannot be negative: {0}", totalFailureThreshold));
		}
		else if (totalFailureThreshold == Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(format("TotalFailureThreshold must be less than MAX_INT: {0}", totalFailureThreshold));
		}
		
		this.totalFailureThreshold = totalFailureThreshold;
	}

	public int getFileFailureThreshold() 
	{
		return fileFailureThreshold;
	}

	public void setFileFailureThreshold(int fileFailureThreshold) 
	{
		if (fileFailureThreshold < 0)
		{
			throw new IllegalArgumentException(format("FileFailureThreshold cannot be negative: {0}", fileFailureThreshold));
		}
		else if (fileFailureThreshold == Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(format("FileFailureThreshold must be less than MAX_INT: {0}", fileFailureThreshold));
		}
		
		this.fileFailureThreshold = fileFailureThreshold;
	}
	
	public ThresholdFailStrategy()
	{
	}
	
	public ThresholdFailStrategy(int fileFailureThreshold, int totalFailureThreshold)
	{
		this.setFileFailureThreshold(fileFailureThreshold);
		this.setTotalFailureThreshold(totalFailureThreshold);
	}

	@Override
	public synchronized FetchFailureAction registerFailure(FetchTarget target, Object context)
	{
		FetchFailureAction action;
		
		if (++this.totalFailures >= this.totalFailureThreshold)
		{
			action = FetchFailureAction.STOP;
		}
		else
		{
			Integer fileFailures = this.failures.get(target.getFilename());
			
			if (fileFailures == null)
			{
				fileFailures = 0;
			}

			this.failures.put(target.getFilename(), ++fileFailures);

			if (fileFailures >= this.fileFailureThreshold)
			{
				action = FetchFailureAction.CONTINUE_WITH_NEXT;
			}
			else
			{
				action = FetchFailureAction.RETRY_CURRENT;
			}
		}
		
		return action;
	}
}
