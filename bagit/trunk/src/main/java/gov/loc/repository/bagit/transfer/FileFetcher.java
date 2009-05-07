package gov.loc.repository.bagit.transfer;

import java.net.URI;

public interface FileFetcher
{
    void fetchFile(URI uri, Long size, FetchedFileDestination destination) throws BagTransferException;
}
