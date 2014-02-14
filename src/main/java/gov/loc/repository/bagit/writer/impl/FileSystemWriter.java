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
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.impl.FileFileNode;
import gov.loc.repository.bagit.filesystem.impl.FileFileSystem;
import gov.loc.repository.bagit.impl.FileBagFile;
import gov.loc.repository.bagit.impl.FileSystemBagFile;
import gov.loc.repository.bagit.utilities.FileHelper;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
/**
 *  Writes a bag to the file system.
 * 
 *  If the bag is being written to the same location, extra files
 *  and directories will be deleted. 
 *  
 *  By default, payload files will not be written if they already exist on disk.
 *  
 *  By default, NFS tmp files will be ignored.
 *  
 *  By default, WriteMode is copy.
 *
 */
public class FileSystemWriter extends AbstractWriter {

	public enum WriteMode { 
		/*
		 * Write from the OutputStream exposed by BagFile.
		 */
		STREAM, 
		/*
		 * When BagFile is a FileBagFile or a FileSystemBagFile with a FileFileNode, write by copying
		 * source file.
		 * 
		 * The file date is preserved.
		 */
		COPY, 
		/*
		 * When BagFile is a FileBagFile or a FileSystemBagFile with a FileFileNode, write by moving
		 * source file.
		 * 
		 * The file date is preserved.
		 */		
		MOVE }
	
	private static final Log log = LogFactory.getLog(FileSystemWriter.class);
	
	private File newBagDir;
	private boolean skipIfPayloadFileExists = true;
	private boolean ignoreNfsTmpFiles = true;
	private Bag origBag;
	private Bag newBag;
	private FileFileSystem fileSystem;
	private int fileTotal = 0;
	private int fileCount = 0;
	private boolean tagFilesOnly = false;
	private boolean filesThatDoNotMatchManifestOnly = false;
	private WriteMode writeMode = WriteMode.COPY;
	
	public FileSystemWriter(BagFactory bagFactory) {
		super(bagFactory);
	}
	
	@Override
	protected Format getFormat() {
		return Format.FILESYSTEM;
	}
	
	/**
	 * Whether to only write files where there is an existing file on disk
	 * and the fixity of the file on disk does not match the fixity recorded in the manifests.
	 * 
	 * When there are multiple fixities recorded for a file, only one is checked.
	 * @param filesThatDoNotMatchManifestOnly True to only write files that exist if they do not match the manifest.
	 */
	public void setFilesThatDoNotMatchManifestOnly(boolean filesThatDoNotMatchManifestOnly) {
		this.filesThatDoNotMatchManifestOnly = filesThatDoNotMatchManifestOnly;
	}
	
	/**
	 * Whether to only write tag files.
	 * @param tagFilesOnly True to only write tag files.
	 */
	public void setTagFilesOnly(boolean tagFilesOnly) {
		this.tagFilesOnly = tagFilesOnly;
	}
	
	
	/**
	 * When removing extra files, whether to ignore NFS tmp files.
	 * 
	 * NFS tmp files start with .nfs. They cannot be deleted.
	 * @param ignore True to ignore NFS tmp files, starting with .nfs
	 */
	public void setIgnoreNfsTmpFiles(boolean ignore) {
		this.ignoreNfsTmpFiles = ignore;
	}
	
	/**
	 * When writing, skip writing a payload file if it already exists.
	 * 
	 * This will make writing a bag much faster.
	 * @param skip True to skip writing a payload file if it already exists.
	 */
	public void setSkipIfPayloadFileExists(boolean skip) {
		this.skipIfPayloadFileExists = skip;
	}

	/**
	 * Sets the write mode for payload files
	 * 
	 * When BagFile is a FileBagFile or a FileSystemBagFile with a FileFileNode 
	 * (that is, there is a source file), determines the mechanism by which the
	 * new file is written.
	 * @param writeMode The {@link WriteMode} to use for the payload writer.
	 */
	public void setPayloadWriteMode(WriteMode writeMode) {
		this.writeMode = writeMode;
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
		this.fileSystem = new FileFileSystem(this.newBagDir);
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
			File sourceFile = null;
			if (bagFile instanceof FileBagFile) {
				sourceFile = ((FileBagFile)bagFile).getFile();
			} else if (bagFile instanceof FileSystemBagFile) {
				FileNode fileNode = ((FileSystemBagFile)bagFile).getFileNode();
				if (fileNode instanceof FileFileNode) {
					sourceFile = ((FileFileNode)fileNode).getFile();
				}
			}
			if (sourceFile != null && WriteMode.COPY.equals(this.writeMode)) {
				if (! file.equals(sourceFile)) {
					log.debug(MessageFormat.format("Copying {0} to {1}", sourceFile, file));
					FileSystemHelper.copy(sourceFile, file);
				} else {
					log.trace(MessageFormat.format("Not copying {0} because source is the same as destination", file));
				}
			} else if (sourceFile != null && WriteMode.MOVE.equals(this.writeMode)) {
				if (! file.equals(sourceFile)) {
					log.debug(MessageFormat.format("Moving {0} to {1}", sourceFile, file));
					FileSystemHelper.move(sourceFile, file);
				} else {
					log.trace(MessageFormat.format("Not moving {0} because source is the same as destination", file));
				}
				
			} else {
				FileSystemHelper.write(bagFile, file);				
			}
		} else {
			log.debug(MessageFormat.format("Skipping writing payload file {0} to {1}.", bagFile.getFilepath(), file.toString()));
		}
		this.newBag.putBagFile(new FileSystemBagFile(bagFile.getFilepath(), this.fileSystem.resolve(bagFile.getFilepath())));
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
		this.newBag.putBagFile(new FileSystemBagFile(bagFile.getFilepath(), this.fileSystem.resolve(bagFile.getFilepath())));
	}
	
	@Override
	public Bag write(Bag bag, File file) {
		log.info("Writing bag");
		this.newBagDir = FileHelper.normalizeForm(file);
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
		for(File file : FileHelper.normalizeForm(dir.listFiles())) {
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
		if (file.exists() && ! checksumMap.isEmpty()) {
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
