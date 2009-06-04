package gov.loc.repository.bagit.transfer.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gov.loc.repository.bagit.transfer.FetchContext;

class FetchStreamCopier
{
	private FetchContext context;
	
	public FetchStreamCopier(FetchContext context, String itemName, int total)
	{
		this.context = context;
	}
	
	public long copy(InputStream in, OutputStream out, String itemName) throws IOException
	{
		byte[] buffer = new byte[4 * 1024];
		long totalCopied = 0;
		long currentTime, lastProgressTime = 0;
		
		int read = in.read(buffer, 0, buffer.length);
		
		while (read >= 0)
		{
			out.write(buffer, 0, read);
			
			// Only update the progress once per second.
			currentTime = System.currentTimeMillis();
			
			if (currentTime - lastProgressTime > 1000)
			{
				lastProgressTime = currentTime;
			}
			
		}				
		
		return totalCopied;
	}
}
