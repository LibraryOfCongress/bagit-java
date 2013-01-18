package gov.loc.repository.bagit.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import gov.loc.repository.bagit.FetchTxt;

/**
 * Represents the target of a fetch.
 * @author Brian Vargas
 */
public class FetchTarget implements Comparable<FetchTarget>
{
	public enum FetchResult {NOT_FETCHED, FETCH_FAILED, VERIFY_FAILED, VERIFY_SUCCEEDED}
	
	private ArrayList<FetchTxt.FilenameSizeUrl> fetchLines;
	private String path;
	private Long size;
	private FetchResult fetchResult = FetchResult.NOT_FETCHED;
	
	public String getFilename()
	{
		return this.path;
	}
	
	public Long getSize()
	{
		return this.size;
	}
	
	public FetchResult getFetchResult()
	{
		return this.fetchResult;
	}
	
	public void setFetchResult(FetchResult fetchResult)
	{
		this.fetchResult = fetchResult;
	}
	
	public List<FetchTxt.FilenameSizeUrl> getLines()
	{
		return Collections.unmodifiableList(this.fetchLines);
	}
	
	public FetchTarget(FetchTxt.FilenameSizeUrl target, FetchResult fetchResult, FetchTxt.FilenameSizeUrl ... targets)
	{
		this.fetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(targets.length + 1);
		this.path = target.getFilename();
		this.size = target.getSize();
		this.fetchResult = fetchResult;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FetchTarget other = (FetchTarget) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public int compareTo(FetchTarget fetchTarget) {
		return this.path.compareTo(fetchTarget.path);
	}
}
