package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.impl.VFSBagFile;
import gov.loc.repository.bagit.utilities.VFSHelper;
import gov.loc.repository.bagit.visitor.AbstractBagVisitor;
import gov.loc.repository.bagit.writer.Writer;

public class FileSystemBagWriter extends AbstractBagVisitor implements Writer {

	private static final Log log = LogFactory.getLog(FileSystemBagWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private File newBagDir;
	private boolean skipIfPayloadFileExists = true;
	private Bag newBag;
	private String newBagURI;
	private CancelIndicator cancelIndicator = null;
	
	public FileSystemBagWriter(File bagDir, boolean skipIfPayloadFileExists) {
		this.skipIfPayloadFileExists = skipIfPayloadFileExists;
		this.newBagDir = bagDir;
	}
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;		
	}
	
	@Override
	public void startBag(Bag bag) {
		try {
			if (newBagDir.exists()) {
				if (! newBagDir.isDirectory()) {
					throw new RuntimeException(MessageFormat.format("Bag directory {0} is not a directory.", newBagDir.toString()));
				}
			} else {
				FileUtils.forceMkdir(newBagDir);
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		this.newBag = BagFactory.createBag(this.newBagDir, bag.getBagConstants().getVersion(), false);
		this.newBagURI = VFSHelper.getUri(this.newBagDir, Format.FILESYSTEM);		
	}
	
	@Override
	public void visitPayload(BagFile bagFile) {
		File file = new File(this.newBagDir, bagFile.getFilepath());
		if (! this.skipIfPayloadFileExists || ! file.exists()) {
			log.debug(MessageFormat.format("Writing payload file {0} to {1}.", bagFile.getFilepath(), file.toString()));
			this.write(bagFile, file);	
		} else {
			log.debug(MessageFormat.format("Skipping writing payload file {0} to {1}.", bagFile.getFilepath(), file.toString()));
		}
		this.newBag.putBagFile(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, bagFile.getFilepath())));
	}
	
	@Override
	public void visitTag(BagFile bagFile) {
		File file = new File(this.newBagDir, bagFile.getFilepath());
		log.debug(MessageFormat.format("Writing tag file {0} to {1}.", bagFile.getFilepath(), file.toString()));		
		this.write(bagFile, file);
		this.newBag.putBagFile(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, bagFile.getFilepath())));
	}
	
	private void write(BagFile bagFile, File file) {
		try {
			File parentDir = file.getParentFile();
			if (! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			
			FileOutputStream out = new FileOutputStream(file);
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				out.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			in.close();
			out.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}

	@Override
	public Bag write(Bag bag) {
		log.info("Writing bag");
		
		bag.accept(this, this.cancelIndicator);
		if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) {
			return null;
		}
		return this.newBag;		

	}
	
}
