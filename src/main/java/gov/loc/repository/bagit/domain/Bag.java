package gov.loc.repository.bagit.domain;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The main representation of the bagit spec.
 */
public final class Bag {
  //The original version of the bag
  private Version version = new Version(-1, -1);
  
  //from the bagit.txt or UTF-8 for new bags
  private Charset fileEncoding = StandardCharsets.UTF_8;
  
  //equivalent to the manifest-<ALG>.txt files
  private Set<Manifest> payLoadManifests = new HashSet<>();
  
  //equivalent to the tagmanifest-<ALG>.txt  files
  private Set<Manifest> tagManifests = new HashSet<>();
  
  //equivalent to the fetch.txt
  private List<FetchItem> itemsToFetch = new ArrayList<>();
  
  //equivalent to the bag-info.txt 
  private List<SimpleImmutableEntry<String, String>> metadata = new ArrayList<>();
  
  //the current location of the bag on the filesystem
  private Path rootDir;
  
  /**
   * empty bag with an invalid version
   */
  public Bag(){
    //intentionally empty
  }
  
  /**
   * empty bag with the specified bag version
   * 
   * @param version the version of the bag
   */
  public Bag(final Version version){
    this.version = version;
  }
  
  /**
   * Create a new bag with the same values as the supplied bag
   * 
   * @param bag the bag to clone
   */
  public Bag(final Bag bag){
    this.version = bag.getVersion();
    this.fileEncoding = bag.fileEncoding;
    this.itemsToFetch = bag.getItemsToFetch();
    this.metadata = bag.getMetadata();
    this.payLoadManifests = bag.getPayLoadManifests();
    this.tagManifests = bag.getTagManifests();
    this.rootDir = bag.getRootDir();
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(95);
    sb.append("Bag [version=").append(version)
    .append(", fileEncoding=").append(fileEncoding)
    .append(", payLoadManifests=[");
    for(final Manifest payloadManifest : payLoadManifests){
      sb.append(payloadManifest).append(' ');
    }
    sb.append("], tagManifests=[");
    for(final Manifest tagManifest : tagManifests){
      sb.append(tagManifest).append(' ');
    }
    sb.append("], itemsToFetch=").append(itemsToFetch)
    .append(", metadata=").append(metadata).append(']');
    
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(version) + Objects.hash(fileEncoding) + Objects.hash(payLoadManifests) + 
        Objects.hash(tagManifests) + Objects.hash(itemsToFetch) + Objects.hash(metadata);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof Bag)){
      return false;
    }
    
    final Bag other = (Bag) obj;
    return Objects.equals(this.version, other.getVersion()) && 
        Objects.equals(this.fileEncoding, other.getFileEncoding()) &&
        Objects.equals(this.payLoadManifests, other.getPayLoadManifests()) && 
        Objects.equals(this.tagManifests, other.getTagManifests()) &&
        Objects.equals(this.itemsToFetch, other.getItemsToFetch()) &&
        Objects.equals(this.metadata, other.getMetadata());
  }
  
  public Version getVersion(){
    return version;
  }

  public Set<Manifest> getPayLoadManifests() {
    return payLoadManifests;
  }

  public void setPayLoadManifests(final Set<Manifest> payLoadManifests) {
    this.payLoadManifests = payLoadManifests;
  }

  public Set<Manifest> getTagManifests() {
    return tagManifests;
  }

  public void setTagManifests(final Set<Manifest> tagManifests) {
    this.tagManifests = tagManifests;
  }

  public List<FetchItem> getItemsToFetch() {
    return itemsToFetch;
  }

  public void setItemsToFetch(final List<FetchItem> itemsToFetch) {
    this.itemsToFetch = itemsToFetch;
  }

  public List<SimpleImmutableEntry<String, String>> getMetadata() {
    return metadata;
  }

  public void setMetadata(final List<SimpleImmutableEntry<String, String>> metadata) {
    this.metadata = metadata;
  }

  public Charset getFileEncoding() {
    return fileEncoding;
  }

  public void setFileEncoding(final Charset fileEncoding) {
    this.fileEncoding = fileEncoding;
  }

  public Path getRootDir() {
    return rootDir;
  }

  public void setRootDir(final Path rootDir) {
    this.rootDir = rootDir;
  }

  public void setVersion(final Version version) {
    this.version = version;
  }

}
