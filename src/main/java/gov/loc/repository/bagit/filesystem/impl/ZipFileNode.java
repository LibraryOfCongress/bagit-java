package gov.loc.repository.bagit.filesystem.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import gov.loc.repository.bagit.filesystem.FileNode;

public class ZipFileNode extends AbstractZipNode implements FileNode {

	private ZipArchiveEntry entry;

	protected ZipFileNode(ZipArchiveEntry entry, String filepath, ZipFileSystem fileSystem) {
		super(filepath, fileSystem);
		this.entry = entry;
	}

	public ZipArchiveEntry getEntry() {
		return this.entry;
	}
	
	@Override
	public long getSize() {
		return this.entry.getSize();
	}

	@Override
	public InputStream newInputStream() {
		if (this.entry == null) {
			throw new RuntimeException("Does not exist");
		}
		
		try {
			return this.fileSystem.getZipfile().getInputStream(this.entry);
		} catch (ZipException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists() {
		return this.entry != null;
	}
	
}
