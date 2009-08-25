package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.BagFile;

import java.io.OutputStream;

public interface FetchedFileDestination
{
    String getFilepath();
    OutputStream openOutputStream(boolean append) throws BagTransferException;
    BagFile commit() throws BagTransferException;
    void abandon();
    
    // Direct access support.
    boolean getSupportsDirectAccess();
    String getDirectAccessPath();
}
