package gov.loc.repository.bagit.domain;

import java.util.Objects;

/**
 * The version of the bagit specification used to create the bag.
 */
public final class Version implements Comparable<Version>{
  public final int major;
  public final int minor;
  
  private transient final String cachedToString;
  
  public Version(final int major, final int minor){
    this.major = major;
    this.minor = minor;
    this.cachedToString = major + "." + minor;
  }
  
  public static Version LATEST_BAGIT_VERSION(){
    return new Version(1, 0);
  }

  @Override
  public String toString() {
    return cachedToString;
  }

  @Override
  public int compareTo(final Version o) {
    //a negative integer - this is less than specified object
    //zero - equal to specified object
    //positive - greater than the specified object
    if(major > o.major || major == o.major && minor > o.minor){
      return 1;
    }
    if(major == o.major && minor == o.minor){
      return 0;
    }
    
    return -1;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major) + Objects.hash(minor);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof Version)){
      return false;
    }
    
    final Version other = (Version) obj;
    
    return Objects.equals(major, other.major) && Objects.equals(minor, other.minor); 
  }
  
  public boolean isNewer(final Version version){
    return this.compareTo(version) > 0;
  }
  
  public boolean isSameOrNewer(final Version version){
    return this.compareTo(version) >= 0;
  }
  
  public boolean isOlder(final Version version){
    return this.compareTo(version) < 0;
  }
  
  public boolean isSameOrOlder(final Version version){
    return this.compareTo(version) <= 0;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }
}
