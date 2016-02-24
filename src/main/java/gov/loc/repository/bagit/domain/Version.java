package gov.loc.repository.bagit.domain;

import java.util.Objects;

public class Version implements Comparable<Version>{
  public final int major;
  public final int minor;
  
  private final String cachedToString;
  
  public Version(int major, int minor){
    this.major = major;
    this.minor = minor;
    this.cachedToString = major + "." + minor;
  }

  @Override
  public String toString() {
    return cachedToString;
  }

  @Override
  public int compareTo(Version o) {
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
  public boolean equals(Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof Version)){
      return false;
    }
    
    Version other = (Version) obj;
    
    return Objects.equals(major, other.major) && Objects.equals(minor, other.minor); 
  }
}
