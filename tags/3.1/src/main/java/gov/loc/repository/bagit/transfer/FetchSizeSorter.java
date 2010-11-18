package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;

import java.util.Comparator;

class FetchSizeSorter extends Object implements Comparator<FetchTxt.FilenameSizeUrl>
{
    @Override
    public int compare(FilenameSizeUrl left, FilenameSizeUrl right)
    {
        Long leftSize = left.getSize();
        Long rightSize = right.getSize();
        int result;
        
        if (leftSize == null)
        {
            if (rightSize == null)
                result = 0;
            else
                result = -1;
        }
        else
        {
            if (rightSize == null)
                result = 1;
            else
                result = leftSize.compareTo(rightSize);
        }
        
        return result;
    }
}
