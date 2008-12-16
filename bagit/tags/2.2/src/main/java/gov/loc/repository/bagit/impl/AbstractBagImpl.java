package gov.loc.repository.bagit.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;


import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.CompletionStrategy;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.VerifyStrategy;
import gov.loc.repository.bagit.completion.DefaultCompletionStrategy;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.FormatHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.VFSHelper;

public abstract class AbstractBagImpl implements Bag {
		
	private static final Log log = LogFactory.getLog(AbstractBagImpl.class);
	
	private Map<String, BagFile> tagMap = new HashMap<String, BagFile>();
	private Map<String, BagFile> payloadMap = new HashMap<String, BagFile>();
	private File bagFile;
	private FileObject bagFileObject = null;
	
	/**
	 * Constructor for an existing bag.
	 * @param file either the bag_dir of a bag on the file system or a serialized bag (zip, tar)
	 */
	public AbstractBagImpl(File file) {
		log.debug(MessageFormat.format("Creating bag for {0}. Version is {1}.", file.toString(), this.getBagConstants().getVersion().toString()));
		this.bagFile = file;
		
		FileObject fileObject = VFSHelper.getFileObject(this.bagFile);
		
		try {
			
			//If a serialized bag, then need to get bag directory from within		
			if (this.getFormat().isSerialized) {
				if (fileObject.getChildren().length != 1) {
					throw new RuntimeException("Unable to find bag_dir in serialized bag");
				}
				this.bagFileObject = fileObject.getChildren()[0];
			}
			else {
				this.bagFileObject = fileObject;	
			}
												
			//Load tag map
			for(FileObject tagFileObject : bagFileObject.getChildren()) {
				if (tagFileObject.getType() == FileType.FILE) {
					
					String filepath = this.bagFileObject.getName().getRelativeName(tagFileObject.getName());
					BagFile bagFile = new VFSBagFile(filepath, tagFileObject);
					if (ManifestHelper.isPayloadManifest(filepath, this.getBagConstants()) || ManifestHelper.isTagManifest(filepath, this.getBagConstants())) {
						tagMap.put(filepath, this.getBagPartFactory().createManifest(filepath, this, bagFile));
					}
					else if (filepath.equals(this.getBagConstants().getBagItTxt())) {
						tagMap.put(filepath, this.getBagPartFactory().createBagItTxt(bagFile));
					}
					else if (filepath.equals(this.getBagConstants().getBagInfoTxt())) {
						tagMap.put(filepath, this.getBagPartFactory().createBagInfoTxt(bagFile));
					}
					else if (filepath.equals(this.getBagConstants().getFetchTxt())) {
						tagMap.put(filepath, this.getBagPartFactory().createFetchTxt(this, bagFile));
					}
					else {
						tagMap.put(filepath, bagFile);	
					}				
				}
			}
			//Find manifests to load payload map
			List<Manifest> payloadManifests = this.getPayloadManifests();
			for(Manifest manifest : payloadManifests) {
				for(String filepath : manifest.keySet()) {
					payloadMap.put(filepath, new VFSBagFile(filepath, this.bagFileObject.resolveFile(filepath)));
				}
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}

	/**
	 * Constructor for a new bag.
	 * Payload should be added to the bag by calling addPayload().
	 */	
	public AbstractBagImpl() {
		log.debug(MessageFormat.format("Creating new bag. Version is {0}.", this.getBagConstants().getVersion().toString()));
	}
	
	public List<Manifest> getPayloadManifests() {
		List<Manifest> manifests = new ArrayList<Manifest>();
		for(BagFile bagFile : this.tagMap.values()) {
			if (bagFile instanceof Manifest) {
				Manifest manifest = (Manifest)bagFile;
				if (manifest.isPayloadManifest()) {
					manifests.add(manifest);
				}
			}
			
		}
		return manifests;			
	}

	public List<Manifest> getTagManifests() {
		List<Manifest> manifests = new ArrayList<Manifest>();
		for(BagFile bagFile : this.tagMap.values()) {
			if (bagFile instanceof Manifest) {
				Manifest manifest = (Manifest)bagFile;
				if (manifest.isTagManifest()) {
					manifests.add(manifest);
				}
			}
			
		}
		return manifests;			
	}
	
	public void removeTagFile(String filepath) {
		if (! this.tagMap.containsKey(filepath)) {
			throw new RuntimeException(MessageFormat.format("Tag file {0} not contained in bag.", filepath));			
		}
		this.tagMap.remove(filepath);		
	}
	
	public void putTagFile(BagFile bagFile) {
		this.tagMap.put(bagFile.getFilepath(), bagFile);		
	}
	
	private void addPayload(File file, File rootDir) {
		//If directory, recurse on children
		if (file.isDirectory()) {
			for(File child : file.listFiles()) {
				this.addPayload(child, rootDir);
			}
				
		} else if (file.isFile()) {
			
			//If file, add to payloadMap
			String filepath = this.getBagConstants().getDataDirectory() + "/";
			if (rootDir != null) {
				filepath += FilenameHelper.removeBasePath(rootDir.toString(), file.toString());
			} else {
				filepath += file.toString();
			}
			if (filepath.indexOf('\\') != -1) {
				throw new UnsupportedOperationException("This Library does not support \\ in filepaths: " + filepath);
			}
			
			log.debug(MessageFormat.format("Adding {0} to payload.", filepath));
			this.payloadMap.put(filepath, new FileBagFile(filepath, file));
		}
		else {
			throw new RuntimeException("Neither a directory or file");
		}

	}

	public void removePayloadFile(String filepath) {
		if (! this.payloadMap.containsKey(filepath)) {
			throw new RuntimeException(MessageFormat.format("Payload file {0} not contained in bag.", filepath));			
		}
		this.payloadMap.remove(filepath);		
	}
	
	public void putPayloadFile(BagFile bagFile) {
		this.payloadMap.put((bagFile.getFilepath()), bagFile);		
	}	
	
	public void addPayload(File file) {
		this.addPayload(file, file.getParentFile());
	}
	
	public void addPayload(List<File> files) {
		for(File file : files) {
			this.addPayload(file);
		}
	}
				
	public SimpleResult checkValid() {
		return null;
	}
	
	public SimpleResult checkComplete() {
		return null;
	}
	
	public void save(File location, Format format) {
		//Note that this may be in place
	}
	
	public Collection<BagFile> getPayloadFiles() {
		return this.payloadMap.values();
	}
	
	public Collection<BagFile> getTagFiles() {
		return this.tagMap.values();
	}
	
	public BagFile getPayloadFile(String filepath) {
		return this.payloadMap.get(filepath);
	}

	public BagFile getTagFile(String filepath) {
		return this.tagMap.get(filepath);
	}
	
	public BagItTxt getBagItTxt() {
		return (BagItTxt)this.getTagFile(this.getBagConstants().getBagItTxt());
	}

	public void setBagItTxt(BagItTxt bagItFile) {
		this.tagMap.put(this.getBagConstants().getBagItTxt(), bagItFile);
		
	}
					
	public SimpleResult isComplete() {
		return this.isComplete(false);
	}
	
	public SimpleResult isComplete(boolean missingBagItTolerant) {
		SimpleResult result = new SimpleResult(true);
		try
		{
			//Is at least one payload manifest		
			if (this.getPayloadManifests().isEmpty()) {
				result.setSuccess(false);
				result.addMessage("Bag does not have any payload manifests");
			}
			//Has bagit file
			if (! missingBagItTolerant && this.getBagItTxt() == null) {
				result.setSuccess(false);
				result.addMessage("Bag does not have " + this.getBagConstants().getBagItTxt());				
			}
			//Bagit is right version
			if (! missingBagItTolerant && this.getBagItTxt() != null && ! this.getBagConstants().getVersion().versionString.equals(this.getBagItTxt().getVersion())) {
				result.setSuccess(false);
				result.addMessage("Version is not " + this.getBagConstants().getVersion());				
			}
			//All payload files are in data directory
			for(String filepath : this.payloadMap.keySet()) {
				if (! filepath.startsWith(this.getBagConstants().getDataDirectory() + '/')) {
					result.setSuccess(false);
					result.addMessage(MessageFormat.format("Payload file {0} not in the {1} directory", filepath, this.getBagConstants().getDataDirectory()));									
				}
			}
			//Every payload BagFile in at least one manifest
			for(String filepath : this.payloadMap.keySet()) {
				boolean inManifest = false;
				for(Manifest manifest : this.getPayloadManifests()) {
					if (manifest.containsKey(filepath)) {
						inManifest = true;
						break;
					}
				}
				if (! inManifest) {
					result.setSuccess(false);
					result.addMessage(MessageFormat.format("Payload file {0} not found in any payload manifest", filepath));														
				}
			}
												
			//Additional checks if an existing Bag
			if (this.bagFileObject != null) {
				//Only directory is a data directory
				for(FileObject fileObject : this.bagFileObject.getChildren())
				{
					if (fileObject.getType() == FileType.FOLDER) {
						String folderName = this.bagFileObject.getName().getRelativeName(fileObject.getName());
						if (! folderName.equals(this.getBagConstants().getDataDirectory())) {
							result.setSuccess(false);
							result.addMessage(MessageFormat.format("Directory {0} not allowed in bag_dir", folderName));
						}
					}
				}
				//If there is a bagFileObject, all payload FileObjects have payload BagFiles
				FileObject dataFileObject = this.bagFileObject.getChild(this.getBagConstants().getDataDirectory());
				if (dataFileObject != null) {
					FileObject[] fileObjects = this.bagFileObject.getChild(this.getBagConstants().getDataDirectory()).findFiles(new FileTypeSelector(FileType.FILE));
					for(FileObject fileObject : fileObjects) {
						
						String filepath = this.bagFileObject.getName().getRelativeName(fileObject.getName());
						if (this.getPayloadFile(filepath) == null) {
							result.setSuccess(false);
							result.addMessage(MessageFormat.format("Bag has file {0} not found in manifest file.", filepath));
						}							
					}
				}
				
			}
		}
		catch(FileSystemException ex) {
			throw new RuntimeException(ex);
		}
		log.info("Completion check: " + result.toString());
		return result;

	}
	
	public SimpleResult isValid() {
		return this.isValid(false);
	}
	
	public SimpleResult isValid(boolean missingBagItTolerant) {
		//Is complete
		SimpleResult result = this.isComplete(missingBagItTolerant);
		if (! result.isSuccess())
		{
			return result;
		}

		//Every checksum checks
		for(Manifest manifest : this.getPayloadManifests()) {			
			result = manifest.isValid();
			if (! result.isSuccess()) {
				log.info("Validity check: " + result.toString());
				return result;
			}
			
		}

		for(Manifest manifest : this.getTagManifests()) {
			result = manifest.isValid();
			if (! result.isSuccess()) {
				log.info("Validity check: " + result.toString());
				return result;
			}
			
		}
		log.info("Validity check: " + result.toString());				
		return result;
	}
	
	public void complete() {
		this.complete(new DefaultCompletionStrategy());
		
	}
	
	public void complete(CompletionStrategy strategy) {
		strategy.complete(this);		
	}
	
	public void write(BagWriter writer) {
		log.info("Writing bag");
		for(String filepath : this.tagMap.keySet()) {
			BagFile bagFile = this.tagMap.get(filepath);
			if (bagFile.exists()) {
				writer.writeTagFile(filepath, bagFile);
			}
		}

		for(String filepath : this.payloadMap.keySet()) {
			BagFile bagFile = this.payloadMap.get(filepath);
			if (bagFile.exists()) {
				writer.writePayloadFile(filepath, bagFile);
			}
		}
		
		writer.close();
		
	}
	
	public void makeHoley(String baseUrl, boolean includePayloadDirectory) {
		log.info("Making bag holey");
		if (! baseUrl.endsWith("/")) {
			baseUrl += "/";
		}
		FetchTxt fetch = this.getBagPartFactory().createFetchTxt(this);
		String[] filepaths = this.payloadMap.keySet().toArray(new String[] {});
		for(String filepath : filepaths) {
			String url = baseUrl;
			if (includePayloadDirectory) {
				url += filepath;
			}
			else {
				url += filepath.substring(this.getBagConstants().getDataDirectory().length() + 1);
			}
			fetch.add(new FetchTxt.FilenameSizeUrl(filepath, this.getPayloadFile(filepath).getSize(), url));
			this.removePayloadFile(filepath);
		}
		this.putFetchTxt(fetch);
	}
		
	public FetchTxt getFetchTxt() {
		return (FetchTxt)this.getTagFile(this.getBagConstants().getFetchTxt());
	}
	
	public void putFetchTxt(FetchTxt fetchTxt) {
		this.tagMap.put(this.getBagConstants().getFetchTxt(), fetchTxt);
		
	}
	
	public Format getFormat() {
		if (this.bagFile == null) {
			return Format.VIRTUAL;
		}
		return FormatHelper.getFormat(this.bagFile);
	}
	
	public BagInfoTxt getBagInfoTxt() {
		return (BagInfoTxt)this.getTagFile(this.getBagConstants().getBagInfoTxt());
	}
	
	public void setBagInfoTxt(BagInfoTxt bagInfoTxt) {
		this.tagMap.put(this.getBagConstants().getBagInfoTxt(), bagInfoTxt);
		
	}
	
	public void setBagInfoTxt(File bagInfoTxtFile) {
		this.setBagInfoTxt(this.getBagPartFactory().createBagInfoTxt(new FileBagFile(this.getBagConstants().getBagInfoTxt(), bagInfoTxtFile)));		
	}

	public SimpleResult additionalVerify(List<VerifyStrategy> strategies) {
		SimpleResult result = new SimpleResult(true);
		for(VerifyStrategy strategy : strategies) {
			result.merge(strategy.verify(this));
		}
		return result;
	}
	
	public SimpleResult additionalVerify(VerifyStrategy strategy) {
		return strategy.verify(this);
	}
}