package gov.loc.repository.bagit.transfer.dest;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.impl.StringBagFile;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;

/**
 * A {@link FetchedFileDestination} that stores data in a byte
 * array in memory.  Useful for unit testing, or for when downloading
 * tag files or other known-to-be-small files.
 * 
 * @version $Id$
 */
public class ByteArrayFetchDestination implements FetchedFileDestination
{
	private String path;
	private ByteArrayOutputStream stream;
	
	public ByteArrayFetchDestination(String path)
	{
		this.path = path;
	}
	
	@Override
	public String getFilepath()
	{
		return this.path;
	}
	
	@Override
	public boolean getSupportsDirectAccess()
	{
		return false;
	}
	
	@Override
	public String getDirectAccessPath()
	{
		throw new IllegalStateException("Direct access is not supported by this fetch destination.");
	}

	@Override
	public void abandon()
	{
		this.stream = null;
	}

	@Override
	public BagFile commit() throws BagTransferException
	{
		if (this.stream == null)
			throw new BagTransferException("No data ever written to destination.");
		
		StringBagFile result = new StringBagFile(this.path, this.stream.toByteArray());
		this.stream = null;
		
		return result;
	}

	@Override
	public OutputStream openOutputStream(boolean append) throws BagTransferException
	{
		return this.stream = new ByteArrayOutputStream();
	}
	
}
