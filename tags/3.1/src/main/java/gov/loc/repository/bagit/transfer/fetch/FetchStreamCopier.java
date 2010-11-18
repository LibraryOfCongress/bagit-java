package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gov.loc.repository.bagit.transfer.BagTransferCancelledException;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

class FetchStreamCopier extends LongRunningOperationBase
{
	private String action;
	private Object item;
	private Long total;
	
	public FetchStreamCopier(String action, Object item, Long total)
	{
		this.action = action;
		this.item = item;
		this.total = total;
	}
	
	public long copy(InputStream in, OutputStream out) throws IOException, BagTransferCancelledException
	{
		byte[] buffer = new byte[4 * 1024];
		long totalCopied = 0;
		
		int read = in.read(buffer, 0, buffer.length);
		
		while (read >= 0)
		{
			out.write(buffer, 0, read);
			totalCopied += read;

			this.progress(this.action, this.item, totalCopied, this.total);
			
			if (this.isCancelled())
				throw new BagTransferCancelledException(format("Copy cancelled after {0} bytes.", totalCopied));
			
			read = in.read(buffer, 0, buffer.length);
		}				
		
		return totalCopied;
	}
}
