package gov.loc.repository.bagit.progresslistener;

import gov.loc.repository.bagit.ProgressListener;
import java.io.Console;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleProgressListener extends Object implements ProgressListener
{
	private Console console = System.console();
	private long nextUpdate = System.currentTimeMillis();
	private int lastLineLength = 0;
	private AtomicBoolean updating = new AtomicBoolean(false);
	
	@Override
	public void reportProgress(String activity, Object item, Long count, Long total)
	{
		if (console != null)
		{
			long now = System.currentTimeMillis();
			long next = this.nextUpdate;
			
			if (now >= next)
			{
				String msg = ProgressListenerHelper.format(activity, item, count, total);

				// We use an atomic boolean here so that we don't have to lock
				// every single time.  This keeps contention down on this
				// bottleneck.
				if (this.updating.compareAndSet(false, true))
				{
					try
					{
						int lastLength = this.lastLineLength;

						this.backup(lastLength);
						this.console.format(msg);
						
						if (msg.length() < lastLength)
						{
							int spacesNeeded = lastLength - msg.length();
							this.spaces(spacesNeeded);
							this.backup(spacesNeeded);
						}
						
						this.console.flush();
						
						this.lastLineLength = msg.length();
						this.nextUpdate = now + 1000;
					}
					finally
					{
						this.updating.set(false);
					}
				}
			}
		}
	}
	
	private void backup(int length)
	{
		for (int i = 0; i < length; i++)
		{
			this.console.format("\b");
		}
	}
	
	private void spaces(int length)
	{
		for (int i = 0; i < length; i++)
		{
			this.console.format(" ");
		}
	}
}
