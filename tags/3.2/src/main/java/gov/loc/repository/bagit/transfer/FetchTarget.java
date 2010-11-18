package gov.loc.repository.bagit.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import gov.loc.repository.bagit.FetchTxt;

/**
 * Represents the target of a fetch.
 * @author Brian Vargas
 */
public class FetchTarget
{
	private ArrayList<FetchTxt.FilenameSizeUrl> fetchLines;
	private String path;
	private Long size;
	
	public String getFilename()
	{
		return this.path;
	}
	
	public Long getSize()
	{
		return this.size;
	}
	
	public List<FetchTxt.FilenameSizeUrl> getLines()
	{
		return Collections.unmodifiableList(this.fetchLines);
	}
	
	public FetchTarget(FetchTxt.FilenameSizeUrl target, FetchTxt.FilenameSizeUrl ... targets)
	{
		this.fetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(targets.length + 1);
		this.path = target.getFilename();
		this.size = target.getSize();

		this.addLine(target);
		
		for (int i = 0; i < targets.length; i++)
		{
			this.addLine(targets[i]);
		}
	}
	
	public void addLine(FetchTxt.FilenameSizeUrl line)
	{
		this.validateLine(line);
		this.fetchLines.add(line);
	}
	
	private void validateLine(FetchTxt.FilenameSizeUrl line)
	{
		if (!line.getFilename().equals(this.path)
				|| (this.size == null && line.getSize() != null)
				|| (this.size != null && !this.size.equals(line.getSize())))
		{
			throw new IllegalArgumentException("All given fetch targets must have the same file name and size.");
		}
	}
}
