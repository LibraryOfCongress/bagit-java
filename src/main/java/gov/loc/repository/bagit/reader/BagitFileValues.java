package gov.loc.repository.bagit.reader;

import java.nio.charset.Charset;

import gov.loc.repository.bagit.domain.Version;

/**
 * A simple data object for passing around all the bagit.txt file values
 */
public class BagitFileValues {
  private final Version version;
  private final Charset encoding;
  private final Long payloadByteCount;
  private final Long payloadFileCount;
  
  public BagitFileValues(final Version version, final Charset encoding, final Long payloadByteCount, final Long payloadFileCount){
    this.version = version;
    this.encoding = encoding;
    this.payloadByteCount = payloadByteCount;
    this.payloadFileCount = payloadFileCount;
  }
  
  public Version getVersion() {
    return version;
  }
  public Charset getEncoding() {
    return encoding;
  }
  public Long getPayloadByteCount() {
    return payloadByteCount;
  }
  public Long getPayloadFileCount() {
    return payloadFileCount;
  }
}
