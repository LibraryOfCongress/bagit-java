package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;

import java.io.Serializable;
import java.util.Comparator;

class FetchFilenameSorter implements Comparator<FetchTxt.FilenameSizeUrl>, Serializable
{
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(FilenameSizeUrl left, FilenameSizeUrl right)
    {
    	return left.getFilename().compareTo(right.getFilename());
    }
}
