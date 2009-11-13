package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;

import java.util.Comparator;

class FetchFilenameSorter extends Object implements Comparator<FetchTxt.FilenameSizeUrl>
{
    @Override
    public int compare(FilenameSizeUrl left, FilenameSizeUrl right)
    {
    	return left.getFilename().compareTo(right.getFilename());
    }
}
