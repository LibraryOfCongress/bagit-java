package gov.loc.repository.bagit.utilities.namevalue;

import gov.loc.repository.bagit.utilities.namevalue.NameValueReader.NameValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NameValueMapList extends Map<String, String>, Iterable<NameValue> {
	/*
	 * Appends name-values.
	 * Any exists name-values with the same key are not touched.
	 */
	void putList(String key, Collection<String> values);

	/*
	 * Appends name-values.
	 * Any exists name-values with the same key are not touched.
	 */
	void putListAll(Collection<NameValue> nameValues);
	
	/*
	 * Appends name-value.
	 * Any exists name-values with the same key are not touched.
	 */	
	void putList(NameValue nameValue);
	
	/*
	 * Appends name-value.
	 * Any exists name-values with the same key are not touched.
	 */		
	void putList(String key, String value);
	
	/*
	 * Same semantics as List.remove()
	 */
	boolean removeList(String key, String value);
	
	/*
	 * Same semantics as List.remove()
	 */
	boolean removeList(NameValue nameValue);
	
	boolean removeAllList(String key);
	
	List<String> getList(String key);
	
	/*
	 * Same semantics as Map.put()
	 */
	String put(NameValue nameValue);
	List<NameValue> asList();
	int sizeList();
}
