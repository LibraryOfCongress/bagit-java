package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.VFSBagFile;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.VFSHelper;

public class FileSystemWriter extends AbstractWriter {

	private static final Log log = LogFactory.getLog(FileSystemWriter.class);
	
	private File newBagDir;
	private boolean skipIfPayloadFileExists = true;
	private boolean ignoreNfsTmpFiles = true;
	private Bag origBag;
	private Bag newBag;
	private String newBagURI;
	private int fileTotal = 0;
	private int fileCount = 0;
	private boolean tagFilesOnly = false;
	private boolean filesThatDoNotMatchManifestOnly = false;
	
	public FileSystemWriter(BagFactory bagFactory) {
		super(bagFactory);
	}
	
	public void setFilesThatDoNotMatchManifestOnly(boolean filesThatDoNotMatchManifestOnly) {
		this.filesThatDoNotMatchManifestOnly = filesThatDoNotMatchManifestOnly;
	}
	
	public void setTagFilesOnly(boolean tagFilesOnly) {
		this.tagFilesOnly = tagFilesOnly;
	}
	
	public void setIgnoreNfsTmpFiles(boolean ignore) {
		this.ignoreNfsTmpFiles = ignore;
	}
	
	public void setSkipIfPayloadFileExists(boolean skip) {
		this.skipIfPayloadFileExists = skip;
	}

	@Override
	protected Format getFormat(File file) {
		return Format.FILESYSTEM;
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
		this.newBag = this.bagFactory.createBag(this.newBagDir, bag.getBagConstants().getVersion(), LoadOption.NO_LOAD);
		this.newBagURI = VFSHelper.getUri(this.newBagDir, Format.FILESYSTEM);
		this.fileCount = 0;
		this.fileTotal = bag.getTags().size() + bag.getPayload().size();
		this.origBag = bag;
	}
	
	@Override
	public void visitPayload(BagFile bagFile) {
		File file = new File(this.newBagDir, bagFile.getFilepath());
		if ((! this.tagFilesOnly) && (! this.skipIfPayloadFileExists || ! file.exists()) && (! this.filesThatDoNotMatchManifestOnly || ! this.fileMatchesManifest(bagFile, file))) {
			this.fileCount++;
			this.progress("writing", bagFile.getFilepath(), this.fileCount, this.fileTotal);
			log.debug(MessageFormat.format("Writing payload file {0} to {1}.", bagFile.getFilepath(), file.toString()));
			FileSystemHelper.write(bagFile, file);	
		} else {
			log.debug(MessageFormat.format("Skipping writing payload file {0} to {1}.", bagFile.getFilepath(), file.toString()));
		}
		this.newBag.putBagFile(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, bagFile.getFilepath())));
	}
	
	@Override
	public void visitTag(BagFile bagFile) {
		this.fileCount++;
		this.progress("writing", bagFile.getFilepath(), this.fileCount, this.fileTotal);
		File file = new File(this.newBagDir, bagFile.getFilepath());
		if (! this.filesThatDoNotMatchManifestOnly || ! this.fileMatchesManifest(bagFile, file)) {
			log.debug(MessageFormat.format("Writing tag file {0} to {1}.", bagFile.getFilepath(), file.toString()));		
			FileSystemHelper.write(bagFile, file);
		} else {
			log.debug(MessageFormat.format("Skipping writing tag file {0} to {1}.", bagFile.getFilepath(), file.toString()));
		}			
		this.newBag.putBagFile(new VFSBagFile(bagFile.getFilepath(), VFSHelper.concatUri(this.newBagURI, bagFile.getFilepath())));
	}
	
	@Override
	public Bag write(Bag bag, File file) {
		log.info("Writing bag");
		this.newBagDir = file;
		bag.accept(this);
		if (this.newBagDir.equals(bag.getFile())) {
			log.debug("Removing any extra files or directories");			
			this.removeExtraFiles(this.newBagDir, false);
			if (! this.tagFilesOnly) {
				//Data directory
				this.removeExtraFiles(new File(this.newBagDir, bag.getBagConstants().getDataDirectory()), true);				
			}
		}
		if (this.isCancelled()) return null;
		return this.newBag;		

	}

	private void removeExtraFiles(File dir, boolean recurse) {
		log.trace(MessageFormat.format("Checking children of {0} for removal", dir));
		for(File file : dir.listFiles()) {
			if (this.isCancelled()) return;
			if (file.isDirectory()) {
				if (log.isTraceEnabled()) {
					log.trace(MessageFormat.format("{0} is a directory with {1} children", file, file.listFiles().length));
				}
				if (recurse) {
					this.removeExtraFiles(file, recurse);
					if (log.isTraceEnabled()) {
						log.trace(MessageFormat.format("{0} now has {1} children", file, file.listFiles().length));
					}
				}
			} else {
				String filepath = FilenameHelper.removeBasePath(this.newBagDir.toString(), file.toString());
				log.trace(MessageFormat.format("{0} is a file whose filepath is {1}", file, filepath));
				if (this.newBag.getBagFile(filepath) == null) {
					if (! this.ignoreNfsTmpFiles || ! file.getName().startsWith(".nfs")) { 
						try {
							log.trace("Deleting " + file);
							FileUtils.forceDelete(file);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					} else {
						log.warn("Ignoring nfs temp file: " + file);
					}
				}				
			}
		}
	}
	
	private boolean fileMatchesManifest(BagFile bagFile, File file) {
		Map<Algorithm, String> checksumMap = this.origBag.getChecksums(bagFile.getFilepath());
		boolean res = false;
		if (! checksumMap.isEmpty()) {
			//Pick an algorithm and check
			java.util.Map.Entry<Algorithm, String> entry = checksumMap.entrySet().iterator().next();
			try {
				res = MessageDigestHelper.fixityMatches(new FileInputStream(file), entry.getKey(), entry.getValue());
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Error reading " + file.getPath(), e);
			}
		}
		log.trace(MessageFormat.format("Result of checking that {0} matches manifest is {1}", bagFile.getFilepath(), res));
		return res;
	}
	
}
