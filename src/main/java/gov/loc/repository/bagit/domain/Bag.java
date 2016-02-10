package gov.loc.repository.bagit.domain;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The main representation of the bagit spec.
 */
public class Bag {
  //The original version of the bag
  private String version;
  
  //from the bagit.txt or UTF-8 for new bags
  private String fileEncoding = StandardCharsets.UTF_8.name();
  
  //equivalent to the manifest-<ALG>.txt files
  private Set<Manifest> payLoadManifests = new HashSet<>();
  
  //equivalent to the tagmanifest-<ALG>.txt  files
  private Set<Manifest> tagManifests = new HashSet<>();
  
  //equivalent to the fetch.txt
  private List<FetchItem> itemsToFetch = new ArrayList<>();
  
  //equivalent to the bag-info.txt 
  private LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
  
  //the current location of the bag on the filesystem
  private File rootDir;
  
  public Bag(){
    version = "0.97";
  }
  
  public Bag(String version){
    this.version = version;
  }
  
  public Bag(Bag bag){
    this.version = bag.getVersion();
    this.fileEncoding = bag.fileEncoding;
    this.itemsToFetch = bag.getItemsToFetch();
    this.metadata = bag.getMetadata();
    this.payLoadManifests = bag.getPayLoadManifests();
    this.tagManifests = bag.getTagManifests();
    this.rootDir = bag.getRootDir();
  }
  
  public String getVersion(){
    return version;
  }

  public Set<Manifest> getPayLoadManifests() {
    return payLoadManifests;
  }

  public void setPayLoadManifests(Set<Manifest> payLoadManifests) {
    this.payLoadManifests = payLoadManifests;
  }

  public Set<Manifest> getTagManifests() {
    return tagManifests;
  }

  public void setTagManifests(Set<Manifest> tagManifests) {
    this.tagManifests = tagManifests;
  }

  public List<FetchItem> getItemsToFetch() {
    return itemsToFetch;
  }

  public void setItemsToFetch(List<FetchItem> itemsToFetch) {
    this.itemsToFetch = itemsToFetch;
  }

  public LinkedHashMap<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(LinkedHashMap<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getFileEncoding() {
    return fileEncoding;
  }

  public void setFileEncoding(String fileEncoding) {
    this.fileEncoding = fileEncoding;
  }

  @Override
  public String toString() {
    return "Bag [version=" + version + ", fileEncoding=" + fileEncoding + ", payLoadManifests=" + payLoadManifests
        + ", tagManifests=" + tagManifests + ", itemsToFetch=" + itemsToFetch + ", metadata=" + metadata + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(version) + Objects.hash(fileEncoding) + Objects.hash(payLoadManifests) + 
        Objects.hash(tagManifests) + Objects.hash(itemsToFetch) + Objects.hash(metadata);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof Bag)){
      return false;
    }
    
    Bag other = (Bag) obj;
    return Objects.equals(this.version, other.getVersion()) && 
        Objects.equals(this.fileEncoding, other.getFileEncoding()) &&
        Objects.equals(this.payLoadManifests, other.getPayLoadManifests()) && 
        Objects.equals(this.tagManifests, other.getTagManifests()) &&
        Objects.equals(this.itemsToFetch, other.getItemsToFetch()) &&
        Objects.equals(this.metadata, other.getMetadata());
  }

  public File getRootDir() {
    return rootDir;
  }

  public void setRootDir(File rootDir) {
    this.rootDir = rootDir;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
