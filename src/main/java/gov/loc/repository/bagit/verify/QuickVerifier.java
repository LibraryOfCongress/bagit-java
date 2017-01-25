package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.InvalidPayloadOxumException;
import gov.loc.repository.bagit.exceptions.PayloadOxumDoesNotExistException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.reader.BagitFileValues;
import gov.loc.repository.bagit.reader.BagitTextFileReader;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * responsible for all things related to quick verification. Quick verification
 * does not mean that a Bag is valid, only that a cursory check has been made.
 * For a full verification see {@link BagVerifier}
 */
public final class QuickVerifier {
  private static final Logger logger = LoggerFactory.getLogger(QuickVerifier.class);
  private static final String PAYLOAD_OXUM_REGEX = "\\d+\\.\\d+";

  private QuickVerifier() {
    // intentionally left empty
  }

  /**
   * Determine if we can quickly verify by comparing the number of files and the
   * total number of bytes expected
   * 
   * @param bag
   *          the {@link Bag} object you wish to check
   *          
   * @return true if the bag can be quickly verified
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public static boolean canQuickVerify(final Bag bag) throws UnparsableVersionException, IOException, InvalidBagMetadataException {
    boolean payloadInfoExists = false;
    final String payloadOxum = getPayloadOxum(bag);
    final BagitFileValues bagitValues = BagitTextFileReader.parseValues(bag.getRootDir().resolve("bagit.txt"));

    if (bagitValues.getPayloadByteCount() != null && bagitValues.getPayloadFileCount() != null) {
      logger.debug("Found payload byte and file count, using that instead of payload-oxum");
      if(payloadOxum != null){
        comparePayloadOxumWithByteAndFileCount(payloadOxum, bagitValues.getPayloadByteCount(), bagitValues.getPayloadFileCount());
      }
      payloadInfoExists = true;
    }

    if (payloadOxum != null && payloadOxum.matches(PAYLOAD_OXUM_REGEX)) {
      logger.debug("Found payload-oxum [{}] for bag [{}]", payloadOxum, bag.getRootDir());
      payloadInfoExists = true;
    }

    return payloadInfoExists && bag.getItemsToFetch().size() == 0;
  }

  /*
   * Get the Payload-Oxum value from the key value pairs
   */
  private static String getPayloadOxum(final Bag bag) {
    for (final SimpleImmutableEntry<String, String> keyValue : bag.getMetadata()) {
      if ("Payload-Oxum".equals(keyValue.getKey())) {
        return keyValue.getValue();
      }
    }
    return null;
  }

  private static void comparePayloadOxumWithByteAndFileCount(final String payloadOxum, final Long payloadByteCount,
      final Long payloadFileCount) {
    final SimpleImmutableEntry<Long, Long> payloadOxumValues = parsePayloadOxum(payloadOxum);
    
    if(!payloadOxumValues.getKey().equals(payloadByteCount)){
      logger.warn("Payload-Oxum byte count [{}] does not match Payload-Byte-Count [{}]!", payloadOxumValues.getKey(), payloadByteCount);
    }
    
    if(!payloadOxumValues.getValue().equals(payloadFileCount)){
      logger.warn("Payload-Oxum file count [{}] does not match Payload-File-Count [{}]!", payloadOxumValues.getValue(), payloadFileCount);
    }
  }

  /**
   * Quickly verify by comparing the number of files and the total number of
   * bytes expected
   * 
   * @param bag the bag to quickly verify
   * @param ignoreHiddenFiles ignore hidden files found in payload directory
   * 
   * @throws IOException if there is an error reading a file
   * @throws InvalidPayloadOxumException
   *           if either the total bytes or the number of files calculated for
   *           the payload directory of the bag is different than the supplied
   *           values
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec 
   * @throws PayloadOxumDoesNotExistException
   *           if the bag does not contain a payload-oxum. To check, run
   *           {@link BagVerifier#canQuickVerify}
   */
  public static void quicklyVerify(final Bag bag, final boolean ignoreHiddenFiles)
      throws IOException, InvalidPayloadOxumException, UnparsableVersionException, InvalidBagMetadataException {
    final SimpleImmutableEntry<Long, Long> byteAndFileCount = getByteAndFileCount(bag);

    final Path payloadDir = PathUtils.getDataDir(bag);
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor(ignoreHiddenFiles);
    Files.walkFileTree(payloadDir, vistor);
    logger.info("supplied payload-oxum: [{}.{}], Calculated payload-oxum: [{}.{}], for payload directory [{}]",
        byteAndFileCount.getKey(), byteAndFileCount.getValue(), vistor.getTotalSize(), vistor.getCount(), payloadDir);

    if (byteAndFileCount.getKey() != vistor.getTotalSize()) {
      throw new InvalidPayloadOxumException(
          "Invalid total size. Expected " + byteAndFileCount.getKey() + " but calculated " + vistor.getTotalSize());
    }
    if (byteAndFileCount.getValue() != vistor.getCount()) {
      throw new InvalidPayloadOxumException(
          "Invalid file count. Expected " + byteAndFileCount.getValue() + " but found " + vistor.getCount() + " files");
    }
  }

  /**
   * get either the payload-oxum values or the payload-byte-count and
   * payload-file-count
   * 
   * @param bag
   *          the bag to get the payload info from
   *          
   * @return the byte count, the file count
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  private static SimpleImmutableEntry<Long, Long> getByteAndFileCount(final Bag bag) throws UnparsableVersionException, IOException, InvalidBagMetadataException {
    final BagitFileValues bagitValues = BagitTextFileReader.parseValues(bag.getRootDir().resolve("bagit.txt"));
    
    if (bagitValues.getPayloadByteCount() != null && bagitValues.getPayloadFileCount() != null) {
      return new SimpleImmutableEntry<Long, Long>(bagitValues.getPayloadByteCount(), bagitValues.getPayloadFileCount());
    }

    final String payloadOxum = getPayloadOxum(bag);
    return parsePayloadOxum(payloadOxum);
  }
  
  private static SimpleImmutableEntry<Long, Long> parsePayloadOxum(final String payloadOxum){
    if (payloadOxum == null || !payloadOxum.matches(PAYLOAD_OXUM_REGEX)) {
      throw new PayloadOxumDoesNotExistException(
          "Payload-Oxum or payload-byte-count and payload-file-count does not exist in bag.");
    }

    final String[] parts = payloadOxum.split("\\.");
    logger.debug("Parsing [{}] for the total byte size of the payload oxum", parts[0]);
    final long totalSize = Long.parseLong(parts[0]);
    logger.debug("Parsing [{}] for the number of files to find in the payload directory", parts[1]);
    final long numberOfFiles = Long.parseLong(parts[1]);
    
    return new SimpleImmutableEntry<>(totalSize, numberOfFiles);
  }
}
