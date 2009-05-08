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
import org.apache.commons.vfs.FileType;


import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagVisitor;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ProgressIndicator;
import gov.loc.repository.bagit.ProgressMonitorable;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.FormatHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.VFSHelper;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.writer.Writer;

public class ConfigurableBag implements Bag {
		
	private static final Log log = LogFactory.getLog(ConfigurableBag.class);
	
	private Map<String, BagFile> tagMap = new HashMap<String, BagFile>();
	private Map<String, BagFile> payloadMap = new HashMap<String, BagFile>();
	private File fileForBag = null;
	private BagPartFactory bagPartFactory = null;
	private BagConstants bagConstants = null;
	
	/**
	 * Constructor for a new bag.
	 * Payload should be added to the bag by calling addPayload().
	 */	
	public ConfigurableBag(BagPartFactory bagPartFactory, BagConstants bagConstants) {
		this.bagPartFactory = bagPartFactory;
		this.bagConstants = bagConstants;
		log.debug(MessageFormat.format("Creating new bag. Version is {0}.", this.getBagConstants().getVersion().toString()));
	}
	
	@Override
	public File getFile() {
		return this.fileForBag;
	}
	
	@Override
	public void setFile(File file) {
		this.fileForBag = file;
		
	}
	
	@Override
	public void load() {
		this.tagMap.clear();
		this.payloadMap.clear();
		
		FileObject bagFileObject = VFSHelper.getFileObjectForBag(this.fileForBag);
		try {													
			//Load tag map
			for(FileObject tagFileObject : bagFileObject.getChildren()) {
				if (tagFileObject.getType() == FileType.FILE) {
					
					String filepath = bagFileObject.getName().getRelativeName(tagFileObject.getName());
					BagFile bagFile = new VFSBagFile(filepath, tagFileObject);
					this.putBagFile(bagFile);
				}
			}
			//Find manifests to load payload map
			List<Manifest> payloadManifests = this.getPayloadManifests();
			for(Manifest manifest : payloadManifests) {
				for(String filepath : manifest.keySet()) {
					BagFile bagFile = new VFSBagFile(filepath, bagFileObject.resolveFile(filepath));
					this.putBagFile(bagFile);
				}
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	@Override
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

	@Override
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
	
	
	@Override
	public void putBagFile(BagFile bagFile) {
		if (BagHelper.isPayload(bagFile.getFilepath(), this.getBagConstants())) {
			this.payloadMap.put(bagFile.getFilepath(), bagFile);
		} else {
			//Is a payload
			if ((! (bagFile instanceof Manifest)) && ManifestHelper.isPayloadManifest(bagFile.getFilepath(), this.getBagConstants()) || ManifestHelper.isTagManifest(bagFile.getFilepath(), this.getBagConstants())) {
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createManifest(bagFile.getFilepath(), bagFile));
			}
			//Is a BagItTxt
			else if ((! (bagFile instanceof BagItTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getBagItTxt())) {
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createBagItTxt(bagFile));
			}
			//Is a BagInfoTxt
			else if ((! (bagFile instanceof BagInfoTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getBagInfoTxt())) {
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createBagInfoTxt(bagFile));
			}
			//Is a FetchTxt
			else if ((! (bagFile instanceof FetchTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getFetchTxt())) {
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createFetchTxt(bagFile));
			}
			else {
				tagMap.put(bagFile.getFilepath(), bagFile);	
			}				

		}
				
	}
	
	@Override
	public void putBagFiles(Collection<BagFile> bagFiles) {
		for(BagFile bagFile : bagFiles) {
			this.putBagFile(bagFile);
		}
		
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
			this.putBagFile(new FileBagFile(filepath, file));
		}
		else {
			throw new RuntimeException("Neither a directory or file");
		}

	}

	@Override
	public void removeBagFile(String filepath) {
		if (BagHelper.isPayload(filepath, this.getBagConstants())) {
			if (! this.payloadMap.containsKey(filepath)) {
				throw new RuntimeException(MessageFormat.format("Payload file {0} not contained in bag.", filepath));			
			}
			this.payloadMap.remove(filepath);					
		} else {
			if (! this.tagMap.containsKey(filepath)) {
				throw new RuntimeException(MessageFormat.format("Tag file {0} not contained in bag.", filepath));			
			}
			this.tagMap.remove(filepath);								
		}
	}
	
	
	@Override
	public void addFilesToPayload(File file) {
		this.addPayload(file, file.getParentFile());
	}
	
	@Override
	public void addFilesToPayload(List<File> files) {
		for(File file : files) {
			this.addFilesToPayload(file);
		}
	}
	
	
	@Override
	public Collection<BagFile> getPayload() {
		return this.payloadMap.values();
	}
	
	@Override
	public Collection<BagFile> getTags() {
		return this.tagMap.values();
	}
	
	@Override
	public BagFile getBagFile(String filepath) {
		if (BagHelper.isPayload(filepath, this.getBagConstants())) {
			return this.payloadMap.get(filepath);
		} else {
			return this.tagMap.get(filepath);
		}
	}
	
	@Override
	public void addFileAsTag(File file) {
		String filepath = file.getName();
		log.debug(MessageFormat.format("Adding {0} to payload.", filepath));
		this.putBagFile(new FileBagFile(filepath, file));
	}
		
	@Override
	public BagItTxt getBagItTxt() {
		return (BagItTxt)this.getBagFile(this.getBagConstants().getBagItTxt());
	}

	@Override
	public SimpleResult checkComplete() {
		return this.checkComplete(null, null);
	}
	
	@Override
	public SimpleResult checkComplete(CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		
		return this.checkAdditionalVerify(this.bagPartFactory.createCompleteVerifier(), cancelIndicator, progressIndicator);
		
	}

	@Override
	public SimpleResult checkValid() {
		return this.checkValid(null, null);
	}

	@Override
	public SimpleResult checkTagManifests() {
		return this.checkTagManifests(null, null);
	}
	
	
	@Override
	public SimpleResult checkTagManifests(CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		ManifestChecksumVerifier verifier = this.bagPartFactory.createManifestVerifier();
		this.setIndicators(verifier, cancelIndicator, progressIndicator);
		return verifier.verify(this.getTagManifests(), this);
	}
	
	@Override
	public SimpleResult checkPayloadManifests() {
		return this.checkPayloadManifests(null, null);
	}
	
	@Override
	public SimpleResult checkPayloadManifests(CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		ManifestChecksumVerifier verifier = this.bagPartFactory.createManifestVerifier();
		this.setIndicators(verifier, cancelIndicator, progressIndicator);
		return verifier.verify(this.getPayloadManifests(), this);
	}
	
	@Override
	public SimpleResult checkValid(CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		return this.invokeVerifier(this.bagPartFactory.createValidVerifier(), cancelIndicator, progressIndicator);
	}
	
	@Override
	public void accept(BagVisitor visitor) {
		this.accept(visitor, null);		
	}
	
	@Override
	public void accept(BagVisitor visitor, CancelIndicator cancelIndicator) {
		
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;
		
		visitor.startBag(this);

		if (cancelIndicator != null && cancelIndicator.performCancel()) return;

		visitor.startTags();
		
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;
		
		for(String filepath : this.tagMap.keySet()) {
			if (cancelIndicator != null && cancelIndicator.performCancel()) return;
			visitor.visitTag(this.tagMap.get(filepath));
		}
		
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;

		visitor.endTags();

		if (cancelIndicator != null && cancelIndicator.performCancel()) return;
		
		visitor.startPayload();
		
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;
		
		for(String filepath : this.payloadMap.keySet()) {
			if (cancelIndicator != null && cancelIndicator.performCancel()) return;
			visitor.visitPayload(this.payloadMap.get(filepath));
		}
		
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;

		visitor.endPayload();
	
		if (cancelIndicator != null && cancelIndicator.performCancel()) return;
		
		visitor.endBag();
	}
			
	@Override
	public FetchTxt getFetchTxt() {
		return (FetchTxt)this.getBagFile(this.getBagConstants().getFetchTxt());
	}
	
	@Override
	public Format getFormat() {
		if (this.fileForBag == null) {
			return Format.VIRTUAL;
		}
		return FormatHelper.getFormat(this.fileForBag);
	}
	
	@Override
	public BagInfoTxt getBagInfoTxt() {
		return (BagInfoTxt)this.getBagFile(this.getBagConstants().getBagInfoTxt());
	}
	
	@Override
	public SimpleResult checkAdditionalVerify(List<Verifier> verifiers) {
		return this.invokeVerifiers(verifiers, null, null);
	}
	
	@Override
	public SimpleResult checkAdditionalVerify(List<Verifier> verifiers, CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		return this.invokeVerifiers(verifiers, cancelIndicator, progressIndicator);
	}
	
	@Override
	public SimpleResult checkAdditionalVerify(Verifier verifier) {
		return this.invokeVerifier(verifier, null, null);
	}
	
	@Override
	public SimpleResult checkAdditionalVerify(Verifier verifier,
			CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		return this.invokeVerifier(verifier, cancelIndicator, progressIndicator);
	}
	
	protected void setIndicators(Object obj, CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		if (cancelIndicator != null && obj instanceof Cancellable) {
			((Cancellable)obj).setCancelIndicator(cancelIndicator);
		}
		if (progressIndicator != null && obj instanceof ProgressMonitorable) {
			((ProgressMonitorable)obj).setProgressIndicator(progressIndicator);
		}
		
	}
	
	protected SimpleResult invokeVerifiers(List<Verifier> verifiers, CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		SimpleResult result = new SimpleResult(true);
		for(Verifier verifier : verifiers) {
			if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
			this.setIndicators(verifier, cancelIndicator, progressIndicator);
			result.merge(verifier.verify(this));
		}		
		return result;
		
		
	}
	
	protected SimpleResult invokeVerifier(Verifier verifier, CancelIndicator cancelIndicator, ProgressIndicator progressIndicator) {
		List<Verifier> verifiers = new ArrayList<Verifier>();
		verifiers.add(verifier);
		return this.invokeVerifiers(verifiers, cancelIndicator, progressIndicator);
	}
	
	@Override
	public BagConstants getBagConstants() {
		return this.bagConstants;
	}
	
	@Override
	public BagPartFactory getBagPartFactory() {
		return this.bagPartFactory;
	}
	
	@Override
	public Bag write(File file, Format format) {
		return this.write(file, format, null, null);
	}
	
	@Override
	public Bag write(File file, Format format, CancelIndicator cancelIndicator,
			ProgressIndicator progressIndicator) {
		Writer writer = this.bagPartFactory.createWriter(format);
		writer.setCancelIndicator(cancelIndicator);
		writer.setProgressIndicator(progressIndicator);
		return writer.write(this, file);
	}
}