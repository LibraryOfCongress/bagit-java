package gov.loc.repository.bagit.driver;

import static java.text.MessageFormat.*;
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
				String msg = format("{0} ({2} of {3}): {1}", activity, item, count == null?"???":count, total == null?"???":total);

				// We use an atomic boolean here so that we don't have to lock
				// every single time.  This keeps contention down on this
				// bottleneck.
				if (this.updating.compareAndSet(false, true))
				{
					try
					{
						int lastLength = this.lastLineLength;
						for (int i = 0; i < lastLength; i++)
						{
							this.console.format("\b");
						}
						
						this.nextUpdate = now + 1000;
						this.console.format(msg);
						this.console.flush();
						
						this.lastLineLength = msg.length();
					}
					finally
					{
						this.updating.set(false);
					}
				}
			}
		}
	}
}
