package gov.loc.repository.bagit.filesystem.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class ZipDirNode extends AbstractZipNode implements DirNode {

	private Map<String, FileSystemNode> childrenMap = new ConcurrentHashMap<String, FileSystemNode>();
	
	protected ZipDirNode(String filepath, ZipFileSystem fileSystem) {
		super(filepath, fileSystem);
	}
	
	public void addChild(FileSystemNode child) {
		childrenMap.put(child.getName(), child);
	}

	@Override
	public Collection<FileSystemNode> listChildren() {
		return Collections.unmodifiableCollection(childrenMap.values());
	}

	@Override
	public FileNode childFile(String name) {
		FileSystemNode child = this.childrenMap.get(name);
		if (child != null && child instanceof FileNode) return (FileNode)child;
		return null;
	}

	@Override
	public DirNode childDir(String name) {
		FileSystemNode child = this.childrenMap.get(name);
		if (child != null && child instanceof DirNode) return (DirNode)child;
		return null;
	}

	@Override
	public Collection<FileSystemNode> listChildren(FileSystemNodeFilter filter) {
		return NodeHelper.listChildren(this, filter);
	}
	
	@Override
	public Collection<FileSystemNode> listDescendants() {
		return NodeHelper.listDescendants(this, null, null);
	}
	
	@Override
	public Collection<FileSystemNode> listDescendants(
			FileSystemNodeFilter filter, FileSystemNodeFilter descentFilter) {
		return NodeHelper.listDescendants(this, filter, descentFilter);
	}

}
