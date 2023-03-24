/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.domain;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class to represent the bag-info.txt (and package-info.txt in older versions)
 */
@SuppressWarnings({"PMD.UseLocaleWithCaseConversions"})
public class Metadata {
  private static final String PAYLOAD_OXUM = "Payload-Oxum";
  private Map<String, List<String>> map = new HashMap<>();
  private List<SimpleImmutableEntry<String, String>> list = new ArrayList<>();
  
  @Override
  public String toString() {
    return list.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj){return true;}
    if (obj == null){return false;}
    if (!(obj instanceof Metadata)){return false;}
    
    final Metadata other = (Metadata) obj;
    return Objects.equals(this.list, other.list);
  }
  
  /**
   * all the metadata
   * 
   * @return return the order and case preserved metadata
   */
  public List<SimpleImmutableEntry<String, String>> getAll(){
    return list;
  }
  
  /**
   * get all the values for a specific label (key)
   * 
   * @param key the case insensitive label(key) in the metadata
   * 
   * @return the list of values for that label
   */
  public List<String> get(final String key){
    return map.get(key.toUpperCase());
  }
  
  /**
   * add a entry into the metadata or append a value if the label already exists
   * 
   * @param key the label
   * @param value the value of the label
   * 
   * @return <samp>true</samp> (as specified by {@link Collection#add})
   */
  public boolean add(final String key, final String value){
    if(PAYLOAD_OXUM.equalsIgnoreCase(key)){
      this.remove(PAYLOAD_OXUM);
    }
    
    final String upperCaseKey = key.toUpperCase();
    if(map.get(upperCaseKey) == null){
      map.put(upperCaseKey, new ArrayList<>());
    }
    map.get(upperCaseKey).add(value);
    
    return list.add(new SimpleImmutableEntry<>(key, value));
  }
  
  /**
   * remove the label and all its values
   * 
   * @param key the label to remove along with its value(s)
   */
  public void remove(final String key){
    map.remove(key.toUpperCase());
    final List<SimpleImmutableEntry<String, String>> newList = new ArrayList<>();
    
    for(final SimpleImmutableEntry<String, String> entry : list){
      if(!entry.getKey().equalsIgnoreCase(key)){
        newList.add(entry);
      }
    }
    list = newList;
  }
  
  /**
   * check if the metadata contains a particular label(key)
   * 
   * @param key the label to check
   * @return if the label exists
   */
  public boolean contains(final String key){
    return map.keySet().contains(key.toUpperCase());
  }
  
  /**
   * add multiple metadata entries
   * 
   * @param data the metadata to add
   */
  public void addAll(final List<SimpleImmutableEntry<String, String>> data){
    for(final SimpleImmutableEntry<String, String> entry : data){
      this.add(entry.getKey(), entry.getValue());
    }
  }
  
  /**
   * payload oxum is a special case where it makes no sense to have multiple values so instead of just appending we upsert (insert or update)
   * @param payloadOxumValue the value payload-oxum should be set to
   * 
   * @return <samp>true</samp> (as specified by {@link Collection#add})
   */
  public boolean upsertPayloadOxum(final String payloadOxumValue){
    map.remove(PAYLOAD_OXUM.toUpperCase());
    SimpleImmutableEntry<String, String> entryToRemove = null;
    for(final SimpleImmutableEntry<String, String> entry : list){
      if(PAYLOAD_OXUM.equalsIgnoreCase(entry.getKey())){
        entryToRemove = entry;
        continue;
      }
    }
    if(entryToRemove != null){
      list.remove(entryToRemove);
    }
    
    return this.add(PAYLOAD_OXUM, payloadOxumValue);
  }
  
  /**
   * @return true if this metadata contains no entries
   */
  public boolean isEmpty(){
    return list.isEmpty();
  }

  protected Map<String, List<String>> getMap() {
    return map;
  }

  protected void setMap(final Map<String, List<String>> map) {
    this.map = map;
  }

  protected List<SimpleImmutableEntry<String, String>> getList() {
    return list;
  }

  protected void setList(final List<SimpleImmutableEntry<String, String>> list) {
    this.list = list;
  }
}
