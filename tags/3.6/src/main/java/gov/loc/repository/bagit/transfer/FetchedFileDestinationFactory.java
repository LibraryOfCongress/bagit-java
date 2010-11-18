package gov.loc.repository.bagit.transfer;

public interface FetchedFileDestinationFactory
{
    FetchedFileDestination createDestination(String path, Long size) throws BagTransferException;
}
