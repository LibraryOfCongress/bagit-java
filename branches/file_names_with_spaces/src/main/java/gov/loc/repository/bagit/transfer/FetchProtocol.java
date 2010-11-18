package gov.loc.repository.bagit.transfer;

import java.net.URI;

public interface FetchProtocol
{
    FileFetcher createFetcher(URI uri, Long size) throws BagTransferException;
}
