package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.FileSystemNode;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipFileSystem implements FileSystem {

	private static final Log log = LogFactory.getLog(ZipFileSystem.class);
	
	private File file;
	private ZipFile zipFile;
	private ZipDirNode root;
	
	public ZipFileSystem(File file) {
		assert file != null;		
		if (! file.isFile()) throw new RuntimeException("Not a file");		
		this.file = file;
		try {
			this.zipFile = new ZipFile(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//Read the directory
		Map<String, ZipDirNode> dirNodeMap = new HashMap<String, ZipDirNode>();
		this.root = new ZipDirNode("", this);
		dirNodeMap.put("", this.root);
		
		Enumeration<ZipArchiveEntry> entryEnum = zipFile.getEntries();
		while(entryEnum.hasMoreElements()) {
			ZipArchiveEntry entry = entryEnum.nextElement();
			String entryFilepath = entry.getName();
			if(entryFilepath.endsWith("/")) entryFilepath = entryFilepath.substring(0, entryFilepath.length()-1);

			//Create the node for the entry			
			File parentFile = new File(entryFilepath).getParentFile();
			List<String> parentPaths = new ArrayList<String>();			
			while(parentFile != null) {
				parentPaths.add((parentFile.getPath()));
				parentFile = parentFile.getParentFile();
			}
			//Can skip if first one already in map
			ZipDirNode parentOfParentDirNode = this.root;
			if (! parentPaths.isEmpty() && dirNodeMap.containsKey(parentPaths.get(0))) {
				parentOfParentDirNode = dirNodeMap.get(parentPaths.get(0));
			} else {
				Collections.reverse(parentPaths);
				//Create the parents
				for(String parentPath : parentPaths) {
					ZipDirNode parentDirNode = dirNodeMap.get(parentPath);
					if (parentDirNode == null) {
						parentDirNode = new ZipDirNode(parentPath, this);
						dirNodeMap.put(parentPath, parentDirNode);					
					}				
					parentOfParentDirNode.addChild(parentDirNode);
					parentOfParentDirNode = parentDirNode;
				}
			}
			//Add
			FileSystemNode entryNode = null;
			if (entry.isDirectory()) {
				entryNode = new ZipDirNode(entryFilepath, this);
			} else {
				entryNode = new ZipFileNode(entry, entry.getName(), this);
			}			
			parentOfParentDirNode.addChild(entryNode);
		}
	}

	
	public ZipFile getZipfile() {
		return this.zipFile;
	}
	
	@Override
	public void close() {
		ZipFile.closeQuietly(this.zipFile);
	}
	
	@Override
	public void closeQuietly() {
		ZipFile.closeQuietly(this.zipFile);
	}

	@Override
	public DirNode getRoot() {
		return this.root;
	}

	@Override
	public File getFile() {
		return this.file;
	}
	
	@Override
	public FileNode resolve(String filepath) {
		log.trace(MessageFormat.format("Resolving {0}", filepath));
		return new ZipFileNode(this.zipFile.getEntry(filepath), filepath, this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.close();
	}
	
	
	
}