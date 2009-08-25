package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class BagFetchResult extends SimpleResult
{
    private Bag resultingBag;
    
    public Bag getResultingBag()
    {
        return this.resultingBag;
    }
    
    public void setResultingBag(Bag bag)
    {
        this.resultingBag = bag;
    }
        
    public BagFetchResult(boolean isSuccess)
    {
        super(isSuccess);
    }

    public BagFetchResult(boolean isSuccess, String message)
    {
        super(isSuccess, message);
    }
}
