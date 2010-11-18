package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.impl.VFSBagFile;
import gov.loc.repository.bagit.utilities.VFSHelper;

public class ZipWriter extends AbstractWriter {

	private static final Log log = LogFactory.getLog(ZipWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private OutputStream out = null;
	private ZipOutputStream zipOut = null;
	private String bagDir = null;
	private String newBagURI = null;
	private Bag newBag = null;
	private File newBagFile = null;
	private List<BagFile> tagBagFiles = new ArrayList<BagFile>();
	private int fileTotal = 0;
	private int fileCount = 0;

	public ZipWriter(BagFactory bagFactory) {
		super(bagFactory);
	}

	public void setBagDir(String bagDir) {
		this.bagDir = bagDir;
	}
		
	@Override
	public void startBag(Bag bag) {
		this.zipOut = new ZipOutputStream(this.out);
		if (this.newBagFile != null) {
			this.newBag = this.bagFactory.createBag(this.newBagFile, bag.getBagConstants().getVersion(), LoadOption.NO_LOAD);
			this.newBagURI = VFSHelper.getUri(this.newBagFile, Format.ZIP);

		}
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
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		if (this.newBagFile != null) {
			this.switchTemp(this.newBagFile);
		}
		if (this.newBag != null) {
			for(BagFile bagFile : this.tagBagFiles) {
				this.newBag.putBagFile(bagFile);
			}
		}
	}

	@Override
	public void visitPayload(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing payload file {0}.", bagFile.getFilepath()));
		this.write(bagFile);
		if (this.newBag != null) {
			this.newBag.putBagFile(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, this.bagDir + "/" + bagFile.getFilepath())));
		}

	}
	
	@Override
	public void visitTag(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing tag file {0}.", bagFile.getFilepath()));
		this.write(bagFile);
		if (this.newBag != null) {
			//Need to delay adding these until after the bag is written
			this.tagBagFiles.add(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, this.bagDir + "/" + bagFile.getFilepath())));
		}

	}
	
	private void write(BagFile bagFile) {
		this.fileCount++;
		this.progress("writing", bagFile.getFilepath(), this.fileCount, this.fileTotal);
		try {
			//Add zip entry
			zipOut.putNextEntry(new ZipEntry(this.bagDir + "/" + bagFile.getFilepath()));
			
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				this.zipOut.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			in.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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
			this.out = new FileOutputStream(this.getTempFile(file));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
		bag.accept(this);
				
		if (this.isCancelled()) return null;
		
		return this.newBag;		
	}

	public Bag write(Bag bag, OutputStream out) {
		this.out = out;
		
		bag.accept(this);
		
		if (this.isCancelled()) return null;
		
		return this.newBag;		
	}

}
