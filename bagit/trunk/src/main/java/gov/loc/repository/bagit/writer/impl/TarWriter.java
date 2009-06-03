package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.impl.VFSBagFile;
import gov.loc.repository.bagit.utilities.VFSHelper;

public class TarWriter extends AbstractWriter {

	public enum Compression { NONE, GZ, BZ2 };
	
	private static final Log log = LogFactory.getLog(TarWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private OutputStream out = null;
	private TarOutputStream tarOut = null;
	private String bagDir = null;
	private String newBagURI = null;
	private Bag newBag = null;
	private File newBagFile = null;
	private List<BagFile> tagBagFiles = new ArrayList<BagFile>(); 
	private int fileTotal = 0;
	private int fileCount = 0;
	private Compression compression = Compression.NONE;

	public TarWriter(BagFactory bagFactory) {
		super(bagFactory);
	}

	public TarWriter(BagFactory bagFactory, Compression compression) {
		super(bagFactory);
		this.compression = compression;
	}
		
	public void setBagDir(String bagDir) {
		this.bagDir = bagDir;
	}
	
	private void init(OutputStream out) {
		try {
			if (Compression.GZ.equals(compression)) {
				this.out = new GZIPOutputStream(out);
			} else if (Compression.BZ2.equals(compression)) {
				out.write('B');
                out.write('Z');
                this.out = new CBZip2OutputStream(out);

			} else {
				this.out = out;
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
		
    @Override
	public void startBag(Bag bag) {
		this.tarOut = new TarOutputStream(this.out);
		if (this.newBagFile != null) {
			this.newBag = this.bagFactory.createBag(this.newBagFile, bag.getBagConstants().getVersion(), LoadOption.NO_LOAD);
		}
		this.fileCount = 0;
		this.fileTotal = bag.getTags().size() + bag.getPayload().size();

	}
	
	@Override
	public void endBag() {
		try {
			if (this.tarOut != null) {
				this.tarOut.close();
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
			this.tagBagFiles.add(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, this.bagDir + "/" + bagFile.getFilepath())));
		}
	}
	
	private void write(BagFile bagFile) {
		this.fileCount++;
		this.progress("writing", bagFile.getFilepath(), this.fileCount, this.fileTotal);
		try {
			//Add tar entry
			TarEntry entry = new TarEntry(this.bagDir + "/" + bagFile.getFilepath());
			entry.setSize(bagFile.getSize());
			tarOut.putNextEntry(entry);
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				this.tarOut.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			tarOut.closeEntry();
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
	
		Format format = Format.TAR;
		if (Compression.GZ.equals(compression)) {
			format = Format.TAR_GZ;
		} else if (Compression.BZ2.equals(compression)) {
			format = Format.TAR_BZ2;
		}
		this.newBagURI = VFSHelper.getUri(file, format);
		
		if (this.bagDir == null) {
			this.bagDir = file.getName().replaceFirst("\\..*$", "");
		}
		
		try {
			File parentDir = file.getParentFile();
			if (parentDir != null && ! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			OutputStream out = new FileOutputStream(this.getTempFile(file));
			this.init(out);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}

		
		bag.accept(this);
		
		if (this.isCancelled()) return null;
		
		return this.newBag;		

	}
	
	public Bag write(Bag bag, OutputStream out) {
		
		this.init(out);
		
		bag.accept(this);
		
		if (this.isCancelled()) return null;
		
		return this.newBag;		

	}
	
}
