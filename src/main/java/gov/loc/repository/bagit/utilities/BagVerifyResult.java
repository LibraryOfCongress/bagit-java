package gov.loc.repository.bagit.utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BagVerifyResult extends SimpleResult {
	protected Set<String> extraPayloadFiles = new HashSet<String>();
	protected Map<String,Set<String>> invalidPayloadFilesMap = new HashMap<String, Set<String>>();
	protected Map<String,Set<String>> invalidTagFilesMap = new HashMap<String, Set<String>>();
	protected Map<String,Set<String>> missingPayloadFilesMap = new HashMap<String, Set<String>>();
	protected Map<String,Set<String>> missingTagFilesMap = new HashMap<String, Set<String>>();

	public BagVerifyResult() {
		super();
	}
	
	public BagVerifyResult(boolean isSuccess) {
		super(isSuccess);
	}
	
	public BagVerifyResult(boolean isSuccess, String message) {
		super(isSuccess, message);
	}

	public BagVerifyResult(boolean isSuccess, Collection<String> messages) {
		super(isSuccess, messages);
	}
	
	public BagVerifyResult(SimpleResult result) {
		super(result.isSuccess(), result.getMessages());
	}
	
	public void addExtraPayloadFile(String filepath) {
		extraPayloadFiles.add(filepath);
	}

	public void addExtraPayloadFiles(Collection<String> filepaths) {
		extraPayloadFiles.addAll(filepaths);
	}
	
	public Set<String> getExtraPayloadFiles() {
		return Collections.unmodifiableSet(extraPayloadFiles);
	}

	public void addInvalidPayloadFile(String manifest, String filepath) {
		Set<String> files = invalidPayloadFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			invalidPayloadFilesMap.put(manifest, files);
		}
		files.add(filepath);
	}

	public void addInvalidPayloadFiles(String manifest, Collection<String> filepaths) {
		Set<String> files = invalidPayloadFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			invalidPayloadFilesMap.put(manifest, files);
		}
		files.addAll(filepaths);
	}
	
	public Set<String> getInvalidPayloadFiles(String manifest) {
		Set<String> files = invalidPayloadFilesMap.get(manifest);
		if (files == null) files = new HashSet<String>();
		return Collections.unmodifiableSet(files);
	}

	public Map<String,Set<String>> getInvalidPayloadFilesMap() {
		return Collections.unmodifiableMap(invalidPayloadFilesMap);
	}

	public Set<String> getInvalidPayloadFiles() {
		Set<String> files = new HashSet<String>();
		for(Set<String> toMergeFiles : invalidPayloadFilesMap.values()) {
			files.addAll(toMergeFiles);
		}
		return files;
	}
	
	public void addInvalidTagFile(String manifest, String filepath) {
		Set<String> files = invalidTagFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			invalidTagFilesMap.put(manifest, files);
		}
		files.add(filepath);
	}

	public void addInvalidTagFiles(String manifest, Collection<String> filepaths) {
		Set<String> files = invalidTagFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			invalidTagFilesMap.put(manifest, files);
		}
		files.addAll(filepaths);
	}

	
	public Set<String> getInvalidTagFiles(String manifest) {
		Set<String> files = invalidTagFilesMap.get(manifest);
		if (files == null) files = new HashSet<String>();
		return Collections.unmodifiableSet(files);
	}

	public Map<String,Set<String>> getInvalidTagFilesMap() {
		return Collections.unmodifiableMap(invalidTagFilesMap);
	}

	public Set<String> getInvalidTagFiles() {
		Set<String> files = new HashSet<String>();
		for(Set<String> toMergeFiles : invalidTagFilesMap.values()) {
			files.addAll(toMergeFiles);
		}
		return files;
	}
	
	public void addMissingPayloadFile(String manifest, String filepath) {
		Set<String> files = missingPayloadFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			missingPayloadFilesMap.put(manifest, files);
		}
		files.add(filepath);
	}

	public void addMissingPayloadFiles(String manifest, Collection<String> filepaths) {
		Set<String> files = missingPayloadFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			missingPayloadFilesMap.put(manifest, files);
		}
		files.addAll(filepaths);
	}
	
	public Set<String> getMissingPayloadFiles(String manifest) {
		Set<String> files = missingPayloadFilesMap.get(manifest);
		if (files == null) files = new HashSet<String>();
		return Collections.unmodifiableSet(files);
	}

	public Map<String,Set<String>> getMissingPayloadFilesMap() {
		return Collections.unmodifiableMap(missingPayloadFilesMap);
	}

	public Set<String> getMissingPayloadFiles() {
		Set<String> files = new HashSet<String>();
		for(Set<String> toMergeFiles : missingPayloadFilesMap.values()) {
			files.addAll(toMergeFiles);
		}
		return files;
	}
	
	public void addMissingTagFile(String manifest, String filepath) {
		Set<String> files = missingTagFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			missingTagFilesMap.put(manifest, files);
		}
		files.add(filepath);
	}

	public void addMissingTagFiles(String manifest, Collection<String> filepaths) {
		Set<String> files = missingTagFilesMap.get(manifest);
		if (files == null) {
			files = new HashSet<String>();
			missingTagFilesMap.put(manifest, files);
		}
		files.addAll(filepaths);
	}

	public Set<String> getMissingTagFiles(String manifest) {
		Set<String> files = missingTagFilesMap.get(manifest);
		if (files == null) files = new HashSet<String>();
		return Collections.unmodifiableSet(files);
	}

	public Map<String,Set<String>> getMissingTagFilesMap() {
		return Collections.unmodifiableMap(missingTagFilesMap);
	}

	public Set<String> getMissingTagFiles() {
		Set<String> files = new HashSet<String>();
		for(Set<String> toMergeFiles : missingTagFilesMap.values()) {
			files.addAll(toMergeFiles);
		}
		return files;
	}

	public boolean isMissingOrInvalid(String filepath) {
		if (contains(missingPayloadFilesMap, filepath)) return true;
		if (contains(missingTagFilesMap, filepath)) return true;
		if (contains(invalidPayloadFilesMap, filepath)) return true;
		if (contains(invalidTagFilesMap, filepath)) return true;
		return false;
	}
	
	private boolean contains(Map<String,Set<String>> map, String filepath) {
		for(Set<String> files : map.values()) {
			if (files.contains(filepath)) return true;
		}
		return false;
	}
	
	@Override
	public void merge(SimpleResult result){
		if(result == null) {
			return;
		}
		super.merge(result);
		if(result instanceof BagVerifyResult) {
			BagVerifyResult bagVerifyResult = (BagVerifyResult)result;
			addExtraPayloadFiles(bagVerifyResult.getExtraPayloadFiles());
			merge(invalidPayloadFilesMap, bagVerifyResult.getInvalidPayloadFilesMap());
			merge(invalidTagFilesMap, bagVerifyResult.getInvalidTagFilesMap());
			merge(missingPayloadFilesMap, bagVerifyResult.getMissingPayloadFilesMap());
			merge(missingTagFilesMap, bagVerifyResult.getMissingTagFilesMap());
		}
	}

	private void merge(Map<String,Set<String>> mergeInto, Map<String,Set<String>> mergeFrom) {
		for(Entry<String, Set<String>> entry : mergeFrom.entrySet()) {
			Set<String> files = mergeInto.get(entry.getKey());
			if (files == null) {
				files = new HashSet<String>();
				mergeInto.put(entry.getKey(), files);
			}
			files.addAll(entry.getValue());
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString());
		append(buf, "Extra payload files", extraPayloadFiles);
		append(buf, "Invalid payload files", invalidPayloadFilesMap);
		append(buf, "Invalid tag files", invalidTagFilesMap);
		append(buf, "Missing payload files", missingPayloadFilesMap);
		append(buf, "Missing tag files", missingTagFilesMap);
		return buf.toString();
	}

	private void append(StringBuffer buf, String name, Map<String, Set<String>> map) {
		if (map.isEmpty()) return;
		for(Entry<String, Set<String>> entry : map.entrySet()) {
			name = name + " from " + entry.getKey();
			append(buf, name, entry.getValue());
		}
	}
	
	private void append(StringBuffer buf, String name, Set<String> files) {
		if (files.isEmpty()) return;
		buf.append(DEFAULT_DELIM);
		buf.append(name);
		buf.append(": ");
		Iterator<String> iter = files.iterator();
		int i=0;
		while(iter.hasNext() && i < DEFAULT_MAX_MESSAGES) {
			if (i > 0) buf.append(", ");
			buf.append(iter.next());
			i++;
		}
		if (i == DEFAULT_MAX_MESSAGES) buf.append(", and others");
		buf.append(".");
	}
}
