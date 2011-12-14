package gov.loc.repository.bagit.filesystem.impl;

import java.util.ArrayList;
import java.util.Collection;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class NodeHelper {
	public static Collection<FileSystemNode> listDescendants(DirNode baseNode, FileSystemNodeFilter filter, FileSystemNodeFilter descentFilter) {
		Collection<FileSystemNode> fileSystemNodes = new ArrayList<FileSystemNode>();
		listDescendants(baseNode, filter, descentFilter, fileSystemNodes);
		return fileSystemNodes;
	}
	
	private static void listDescendants(DirNode baseNode, FileSystemNodeFilter filter, FileSystemNodeFilter descentFilter, Collection<FileSystemNode> fileSystemNodes) {
		for(FileSystemNode child : baseNode.listChildren()) {
			if (filter == null || filter.accept(child)) {
				fileSystemNodes.add(child);
			}
			if(child instanceof DirNode && (descentFilter == null || descentFilter.accept(child))) {
				listDescendants((DirNode)child, filter, descentFilter, fileSystemNodes);
			}
		}
	}

	public static Collection<FileSystemNode> listChildren(DirNode baseNode, FileSystemNodeFilter filter) {
		Collection<FileSystemNode> fileSystemNodes = new ArrayList<FileSystemNode>();
		listChildren(baseNode, filter, fileSystemNodes);
		return fileSystemNodes;
		
	}

	
	private static void listChildren(DirNode baseNode, FileSystemNodeFilter filter, Collection<FileSystemNode> fileSystemNodes) {
		for(FileSystemNode child : baseNode.listChildren()) {
			if (filter == null || filter.accept(child)) {
				fileSystemNodes.add(child);
			}
		}
	}

}
