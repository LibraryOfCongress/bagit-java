package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.impl.ZipFileSystem;
import gov.loc.repository.bagit.impl.FileSystemBagFile;

public class ZipWriter extends AbstractWriter {

	public static final int DEFAULT_COMPRESSION_LEVEL = 1;
	
	private static final Log log = LogFactory.getLog(ZipWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private ZipArchiveOutputStream zipOut = null;
	private String bagDir = null;
	private Bag newBag = null;
	private File newBagFile = null;
	private List<String> filepaths = new ArrayList<String>();
	private int fileTotal = 0;
	private int fileCount = 0;
	private File tempFile;
	private Integer compressionLevel = null;

	public ZipWriter(BagFactory bagFactory) {
		super(bagFactory);
	}

	public void setBagDir(String bagDir) {
		this.bagDir = bagDir;
	}

	public void setCompressionLevel(Integer compressionLevel) {
		if (compressionLevel != null && (compressionLevel < 0 || compressionLevel > 9))
			throw new RuntimeException("Valid compression levels are 0-9.");
		this.compressionLevel = compressionLevel;
	}
	
	@Override
	protected Format getFormat() {
		return Format.ZIP;
	}
	
	@Override
	public void startBag(Bag bag) {
		try {
			this.zipOut = new ZipArchiveOutputStream(this.tempFile);
			this.zipOut.setLevel(ZipArchiveOutputStream.STORED);
			if (this.compressionLevel != null) {
				this.zipOut.setLevel(this.compressionLevel * -1);
				this.zipOut.setMethod(ZipOutputStream.DEFLATED);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.newBag = this.bagFactory.createBag(this.newBagFile, bag.getBagConstants().getVersion(), LoadOption.NO_LOAD);
		this.fileCount = 0;
		this.fileTotal = bag.getTags().size() + bag.getPayload().size();
	}
	
	@Override
	public void endBag() {
		try {
			if (this.zipOut != null) {
				this.zipOut.close();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		this.switchTemp(this.newBagFile);
		FileSystem fileSystem = new ZipFileSystem(this.newBagFile);
		for(String filepath : filepaths) {
			this.newBag.putBagFile(new FileSystemBagFile(filepath, fileSystem.resolve(this.bagDir + "/" + filepath)));
		}
	}

	@Override
	public void visitPayload(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing payload file {0}.", bagFile.getFilepath()));
		this.write(bagFile);
	}
	
	@Override
	public void visitTag(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing tag file {0}.", bagFile.getFilepath()));
		this.write(bagFile);
	}
	
	private void write(BagFile bagFile) {
		this.fileCount++;
		this.progress("writing", bagFile.getFilepath(), this.fileCount, this.fileTotal);
		//Add zip entry
		try {
			ZipArchiveEntry entry = new ZipArchiveEntry(this.bagDir + "/" + bagFile.getFilepath());
			entry.setSize(bagFile.getSize());
			zipOut.putArchiveEntry(entry);
			
			InputStream in = bagFile.newInputStream();
			try {
				byte[] dataBytes = new byte[BUFFERSIZE];
				int nread = in.read(dataBytes);
				while (nread > 0) {
					zipOut.write(dataBytes, 0, nread);
				    nread = in.read(dataBytes);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
			zipOut.closeArchiveEntry();
			
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		this.filepaths.add(bagFile.getFilepath());
	}
	
	@Override
	public Bag write(Bag bag, File file) {
		log.info("Writing bag");
		
		this.newBagFile = file;
		if (this.bagDir == null) {
			this.bagDir = file.getName().replaceFirst("\\..*$", "");	
		}
		
		try {
			File parentDir = file.getParentFile();
			if (parentDir != null && ! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			this.tempFile = this.getTempFile(file);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
		bag.accept(this);
				
		if (this.isCancelled()) return null;
		
		return this.newBag;		
	}

}
