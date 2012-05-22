package gov.loc.repository.bagit.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.Closeable;
import java.io.File;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagVisitor;
import gov.loc.repository.bagit.DeclareCloseable;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystemFactory;
import gov.loc.repository.bagit.filesystem.FileSystemFactory.UnsupportedFormatException;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.filter.FileNodeFileSystemNodeFilter;
import gov.loc.repository.bagit.filesystem.filter.IgnoringFileSystemNodeFilter;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.transformer.impl.HolePuncherImpl;
import gov.loc.repository.bagit.utilities.CancelUtil;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.FormatHelper;
import gov.loc.repository.bagit.utilities.FormatHelper.UnknownFormatException;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.verify.FailModeSupporting.FailMode;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.Writer;

public abstract class AbstractBag implements Bag {
		
	private static final Log log = LogFactory.getLog(AbstractBag.class);
	
	private Map<String, BagFile> tagMap = new HashMap<String, BagFile>();
	private Map<String, BagFile> payloadMap = new HashMap<String, BagFile>();
	private File fileForBag = null;
	private BagPartFactory bagPartFactory = null;
	private BagConstants bagConstants = null;
	private BagFactory bagFactory = null;
	private Set<Closeable> closeables = new HashSet<Closeable>();
	private volatile boolean isClosed = false;
	
	/**
	 * Constructor for a new bag.
	 * Payload should be added to the bag by calling addPayload().
	 */	
	public AbstractBag(BagPartFactory bagPartFactory, BagConstants bagConstants, BagFactory bagFactory) {
		this.bagPartFactory = bagPartFactory;
		this.bagConstants = bagConstants;
		this.bagFactory = bagFactory;
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
	public Version getVersion() {
		return this.getBagConstants().getVersion();
	}
	
	@Override
	public void loadFromManifests() {
		log.debug(MessageFormat.format("Loading from {0} using payload manifests", this.fileForBag));
		this.tagMap.clear();
		this.payloadMap.clear();
		
		DirNode bagFileDirNode;
		try {
			bagFileDirNode = FileSystemFactory.getDirNodeForBag(this.fileForBag);
		} catch (UnknownFormatException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedFormatException e) {
			throw new RuntimeException(e);
		}
		log.trace(MessageFormat.format("BagFileDirNode has filepath {0} and is a {1}", bagFileDirNode.getFilepath(), bagFileDirNode.getClass().getSimpleName()));
		
		//Load root tag map
		for(FileSystemNode node : bagFileDirNode.listChildren()) {
			if (node instanceof FileNode) {
				FileNode tagFileNode = (FileNode)node;
				String filepath = FilenameHelper.removeBasePath(bagFileDirNode.getFilepath(), tagFileNode.getFilepath());
				log.trace(MessageFormat.format("Loading tag {0} using filepath {1}", tagFileNode.getFilepath(), filepath));
				BagFile bagFile = new FileSystemBagFile(filepath, tagFileNode);
				this.putBagFile(bagFile);
			}
		}

		//Find manifests to load tag map
		List<Manifest> tagManifests = this.getTagManifests();
		for(Manifest manifest : tagManifests) {
			for(String filepath : manifest.keySet()) {
				String fullFilepath = FilenameHelper.concatFilepath(bagFileDirNode.getFilepath(), filepath);
				FileNode tagFileNode = bagFileDirNode.getFileSystem().resolve(fullFilepath);
				BagFile bagFile = new FileSystemBagFile(filepath, tagFileNode);
				log.trace(MessageFormat.format("Loading tag {0} from {1} using filepath {2}", tagFileNode.getFilepath(), manifest.getFilepath(), filepath));
				this.putBagFile(bagFile);
			}
		}
		
		//Find manifests to load payload map
		List<Manifest> payloadManifests = this.getPayloadManifests();
		for(Manifest manifest : payloadManifests) {
			for(String filepath : manifest.keySet()) {
				String fullFilepath = FilenameHelper.concatFilepath(bagFileDirNode.getFilepath(), filepath);
				FileNode payloadFileNode = bagFileDirNode.getFileSystem().resolve(fullFilepath);
				BagFile bagFile = new FileSystemBagFile(filepath, payloadFileNode);
				log.trace(MessageFormat.format("Loading payload {0} from {1} using filepath {2}", payloadFileNode.getFilepath(), manifest.getFilepath(), filepath));
				this.putBagFile(bagFile);
			}
		}
	}

	@Override
	public void loadFromFiles() {
		this.loadFromFiles(new ArrayList<String>());
	}
	
	@Override
	public void loadFromFiles(List<String> ignoreAdditionalDirectories) {
		log.debug(MessageFormat.format("Loading from {0} using payload files", this.fileForBag));

		this.tagMap.clear();
		this.payloadMap.clear();
		
		DirNode bagFileDirNode;
		try {
			bagFileDirNode = FileSystemFactory.getDirNodeForBag(this.fileForBag);
		} catch (UnknownFormatException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedFormatException e) {
			throw new RuntimeException(e);
		}
		log.trace(MessageFormat.format("BagFileDirNode has filepath {0} and is a {1}", bagFileDirNode.getFilepath(), bagFileDirNode.getClass().getSimpleName()));
		
		IgnoringFileSystemNodeFilter descentFilter = new IgnoringFileSystemNodeFilter(ignoreAdditionalDirectories, false);
		descentFilter.setRelativeFilepath(bagFileDirNode.getFilepath());
		Collection<FileSystemNode> nodes = bagFileDirNode.listDescendants(new FileNodeFileSystemNodeFilter(), descentFilter);
		log.trace(MessageFormat.format("{0} files found", nodes.size()));
		
		for(FileSystemNode node : nodes) {
			log.trace("Reading " + node.getFilepath());
			String filepath = FilenameHelper.removeBasePath(bagFileDirNode.getFilepath(), node.getFilepath());
			BagFile bagFile = new FileSystemBagFile(filepath, (FileNode)node);
			log.trace(MessageFormat.format("Loading {0} using filepath {1}", node.getFilepath(), filepath));
			this.putBagFile(bagFile);			
		}			
	}

	
	@Override
	public List<Manifest> getPayloadManifests() {
		log.debug("Getting payload manifests");
		checkClosed();
		
		List<Manifest> manifests = new ArrayList<Manifest>();		
		for(BagFile bagFile : this.tagMap.values()) {
			log.trace(MessageFormat.format("Checking if {0} is a payload manifest", bagFile.getFilepath()));
			if (bagFile instanceof Manifest) {
				log.trace(MessageFormat.format("{0} is a manifest", bagFile.getFilepath()));
				Manifest manifest = (Manifest)bagFile;
				if (manifest.isPayloadManifest()) {
					log.trace(MessageFormat.format("{0} is a payload manifest", bagFile.getFilepath()));
					manifests.add(manifest);
				}
			}
			
		}
		return manifests;			
	}

	@Override
	public List<Manifest> getTagManifests() {
		log.debug("Getting tag manifests");
		checkClosed();

		List<Manifest> manifests = new ArrayList<Manifest>();
		for(BagFile bagFile : this.tagMap.values()) {
			log.trace(MessageFormat.format("Checking if {0} is a tag manifest", bagFile.getFilepath()));
			if (bagFile instanceof Manifest) {
				log.trace(MessageFormat.format("{0} is a manifest", bagFile.getFilepath()));
				Manifest manifest = (Manifest)bagFile;
				if (manifest.isTagManifest()) {
					log.trace(MessageFormat.format("{0} is a tag manifest", bagFile.getFilepath()));
					manifests.add(manifest);
				}
			}
			
		}
		return manifests;			
	}
	
	
	@Override
	public void putBagFile(BagFile bagFile) {
		checkClosed();

		log.trace(MessageFormat.format("Putting bag file {0}", bagFile.getFilepath()));
		
		if (bagFile instanceof DeclareCloseable) {
			this.closeables.add(((DeclareCloseable)bagFile).declareCloseable());
		}
		
		if (BagHelper.isPayload(bagFile.getFilepath(), this.getBagConstants())) {
			log.trace(MessageFormat.format("Adding bag file {0} to payload map", bagFile.getFilepath()));
			this.payloadMap.put(bagFile.getFilepath(), bagFile);
		} else {
			//Is a Manifest
			if (bagFile.exists() && (! (bagFile instanceof Manifest)) && (ManifestHelper.isPayloadManifest(bagFile.getFilepath(), this.getBagConstants()) || ManifestHelper.isTagManifest(bagFile.getFilepath(), this.getBagConstants()))) {
				log.trace(MessageFormat.format("Adding bag file {0} to tag map as a Manifest", bagFile.getFilepath()));
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createManifest(bagFile.getFilepath(), bagFile));
			}
			//Is a BagItTxt
			else if (bagFile.exists() && (! (bagFile instanceof BagItTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getBagItTxt())) {
				log.trace(MessageFormat.format("Adding bag file {0} to tag map as a BagItTxt", bagFile.getFilepath()));
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createBagItTxt(bagFile));
			}
			//Is a BagInfoTxt
			else if (bagFile.exists() && (! (bagFile instanceof BagInfoTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getBagInfoTxt())) {
				log.trace(MessageFormat.format("Adding bag file {0} to tag map as a BagInfoTxt", bagFile.getFilepath()));
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createBagInfoTxt(bagFile));
			}
			//Is a FetchTxt
			else if (bagFile.exists() && (! (bagFile instanceof FetchTxt)) && bagFile.getFilepath().equals(this.getBagConstants().getFetchTxt())) {
				log.trace(MessageFormat.format("Adding bag file {0} to tag map as a FetchTxt", bagFile.getFilepath()));
				tagMap.put(bagFile.getFilepath(), this.getBagPartFactory().createFetchTxt(bagFile));
			}
			else {
				log.trace(MessageFormat.format("Adding bag file {0} to tag map", bagFile.getFilepath()));
				tagMap.put(bagFile.getFilepath(), bagFile);	
			}				

		}
				
	}
	
	@Override
	public void putBagFiles(Collection<BagFile> bagFiles) {
		checkClosed();
		for(BagFile bagFile : bagFiles) {
			this.putBagFile(bagFile);
		}
		
	}
		
	@Override
	public void removeBagFile(String filepath) {
		checkClosed();
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
	public void addFileToPayload(File file) {
		checkClosed();
		
		new AddFilesToPayloadOperation(this).addFileToPayload(file);
	}
	
	@Override
	public void addFilesToPayload(List<File> files) {
		checkClosed();
		new AddFilesToPayloadOperation(this).addFilesToPayload(files);
	}
	
	
	@Override
	public Collection<BagFile> getPayload() {
		checkClosed();
		return this.payloadMap.values();
	}
	
	@Override
	public Collection<BagFile> getTags() {
		checkClosed();
		return this.tagMap.values();
	}
	
	@Override
	public BagFile getBagFile(String filepath) {
		checkClosed();
		
		if (BagHelper.isPayload(filepath, this.getBagConstants())) {
			return this.payloadMap.get(filepath);
		} else {
			return this.tagMap.get(filepath);
		}
	}
	
	@Override
	public void addFileAsTag(File file) {
		checkClosed();
		new AddFilesToTagsOperation(this).addFileToTags(file);
	}

	@Override
	public void addFilesAsTag(List<File> files) {
		checkClosed();
		new AddFilesToTagsOperation(this).addFilesToTags(files);
	}

	
	@Override
	public BagItTxt getBagItTxt() {
		checkClosed();
		BagFile bagFile = this.getBagFile(this.getBagConstants().getBagItTxt());
		if (bagFile != null && bagFile instanceof BagItTxt) return (BagItTxt)bagFile;
		return null;
	}

	@Override
	public SimpleResult verifyComplete() {
		return this.verifyComplete(FailMode.FAIL_STAGE);
	}
	
	@Override
	public SimpleResult verifyComplete(FailMode failMode) {
		checkClosed();
		CompleteVerifierImpl verifier = new CompleteVerifierImpl();
		verifier.setFailMode(failMode);
		return verifier.verify(this);
	}
	
	@Override
	public SimpleResult verifyTagManifests() {		
		return this.verifyTagManifests(FailMode.FAIL_STAGE);
	}
	
	@Override
	public SimpleResult verifyTagManifests(FailMode failMode) {
		checkClosed();
		ParallelManifestChecksumVerifier verifier = new ParallelManifestChecksumVerifier();
		verifier.setFailMode(failMode);
		return verifier.verify(this.getTagManifests(), this);
	}
	
	
	@Override
	public SimpleResult verifyPayloadManifests() {
		return this.verifyPayloadManifests(FailMode.FAIL_STAGE);
	}

	@Override
	public SimpleResult verifyPayloadManifests(FailMode failMode) {
		checkClosed();
		ParallelManifestChecksumVerifier verifier = new ParallelManifestChecksumVerifier();
		verifier.setFailMode(failMode);
		return verifier.verify(this.getPayloadManifests(), this);
	}
	
	@Override
	public SimpleResult verifyValid() {
		return this.verifyValid(FailMode.FAIL_STAGE);
	}
	
	@Override
	public SimpleResult verifyValid(FailMode failMode) {
		checkClosed();
		ValidVerifierImpl verifier = new ValidVerifierImpl(new CompleteVerifierImpl(), new ParallelManifestChecksumVerifier());
		verifier.setFailMode(failMode);
		return verifier.verify(this);
	}
	
	@Override
	public void accept(BagVisitor visitor) {
		checkClosed();
		if (CancelUtil.isCancelled(visitor)) return;
		
		visitor.startBag(this);

		if (CancelUtil.isCancelled(visitor)) return;

		visitor.startTags();
		
		if (CancelUtil.isCancelled(visitor)) return;
		
		for(String filepath : this.tagMap.keySet()) {
			if (CancelUtil.isCancelled(visitor)) return;
			visitor.visitTag(this.tagMap.get(filepath));
		}
		
		if (CancelUtil.isCancelled(visitor)) return;

		visitor.endTags();

		if (CancelUtil.isCancelled(visitor)) return;
		
		visitor.startPayload();
		
		if (CancelUtil.isCancelled(visitor)) return;
		
		for(String filepath : this.payloadMap.keySet()) {
			if (CancelUtil.isCancelled(visitor)) return;
			visitor.visitPayload(this.payloadMap.get(filepath));
		}
		
		if (CancelUtil.isCancelled(visitor)) return;

		visitor.endPayload();
	
		if (CancelUtil.isCancelled(visitor)) return;
		
		visitor.endBag();
	}
			
	@Override
	public FetchTxt getFetchTxt() {
		checkClosed();
		BagFile bagFile = this.getBagFile(this.getBagConstants().getFetchTxt());
		if (bagFile != null && bagFile instanceof FetchTxt) return (FetchTxt)bagFile;
		return null;
	}
	
	@Override
	public Format getFormat() {
		if (this.fileForBag == null) {
			return null;
		}
		try {
			return FormatHelper.getFormat(this.fileForBag);
		} catch (UnknownFormatException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public BagInfoTxt getBagInfoTxt() {
		checkClosed();
		BagFile bagFile = this.getBagFile(this.getBagConstants().getBagInfoTxt());
		if (bagFile != null && bagFile instanceof BagInfoTxt) return (BagInfoTxt)bagFile;
		return null;
	}
			
	@Override
	public SimpleResult verify(Verifier verifier) {
		checkClosed();
		return verifier.verify(this);
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
	public Bag write(Writer writer, File file) {
		checkClosed();
		return writer.write(this, file);
	}
		
	@Override
	public void removePayloadDirectory(String filepath) {
		checkClosed();
		if (! filepath.endsWith("/")) {
			filepath += "/";
		}
		if (! filepath.startsWith(bagConstants.getDataDirectory())) {
			filepath = bagConstants.getDataDirectory() + "/" + filepath;
		}
		
		if ((bagConstants.getDataDirectory() + "/").equals(filepath)) {
			return;
		}

		log.debug("Removing payload directory " + filepath);
		
		List<String> deleteFilepaths = new ArrayList<String>();
		
		for(BagFile bagFile : this.getPayload()) {
			if (bagFile.getFilepath().startsWith(filepath)) {
				deleteFilepaths.add(bagFile.getFilepath());
			}
		}
		
		for(String deleteFilepath : deleteFilepaths) {
			log.debug("Removing " + deleteFilepath);
			this.removeBagFile(deleteFilepath);			
		}
	}

	@Override
	public void removeTagDirectory(String filepath) {
		checkClosed();
		if (! filepath.endsWith("/")) {
			filepath += "/";
		}
		if (filepath.startsWith(bagConstants.getDataDirectory())) {
			throw new RuntimeException("Trying to remove payload");
		}
		
		log.debug("Removing tag directory " + filepath);
		
		List<String> deleteFilepaths = new ArrayList<String>();
		
		for(BagFile bagFile : this.getTags()) {
			if (bagFile.getFilepath().startsWith(filepath)) {
				deleteFilepaths.add(bagFile.getFilepath());
			}
		}
		
		for(String deleteFilepath : deleteFilepaths) {
			log.debug("Removing " + deleteFilepath);
			this.removeBagFile(deleteFilepath);			
		}
	}

	
	@Override
	public Map<Algorithm, String> getChecksums(String filepath) {
		checkClosed();
		Map<Algorithm, String> checksumMap = new HashMap<Algorithm, String>();
		if (BagHelper.isPayload(filepath, this.bagConstants)) {
			for(Manifest manifest : this.getPayloadManifests()) {
				if (manifest.containsKey(filepath)) {
					checksumMap.put(manifest.getAlgorithm(), manifest.get(filepath));
				}
			}
		} else {
			for(Manifest manifest : this.getTagManifests()) {
				if (manifest.containsKey(filepath)) {
					checksumMap.put(manifest.getAlgorithm(), manifest.get(filepath));
				}
			}
			
		}		
		return checksumMap;
	}
	
	@Override
	public Manifest getPayloadManifest(Algorithm algorithm) {
		checkClosed();
		BagFile bagFile = this.getBagFile(ManifestHelper.getPayloadManifestFilename(algorithm, this.bagConstants));
		if (bagFile != null && bagFile instanceof Manifest) return (Manifest)bagFile;
		return null;
	}
	
	@Override
	public Manifest getTagManifest(Algorithm algorithm) {
		checkClosed();
		BagFile bagFile = this.getBagFile(ManifestHelper.getTagManifestFilename(algorithm, this.bagConstants));
		if (bagFile != null && bagFile instanceof Manifest) return (Manifest)bagFile;
		return null;
	}
	
	@Override
	public Bag makeComplete(Completer completer) {
		checkClosed();
		return completer.complete(this);
	}
	
	@Override
	public Bag makeComplete() {
		Completer completer = new DefaultCompleter(this.bagFactory);
		return completer.complete(this);
	}
	
	@Override
	public Bag makeHoley(HolePuncher holePuncher, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean resume) {
		checkClosed();
		return holePuncher.makeHoley(this, baseUrl, includePayloadDirectoryInUrl, includeTags, resume);
	}
	
	@Override
	public Bag makeHoley(String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean resume) {
		checkClosed();
		HolePuncher holePuncher = new HolePuncherImpl(this.bagFactory);
		return holePuncher.makeHoley(this, baseUrl, includePayloadDirectoryInUrl, includeTags, resume);
	}
	
	@Override
	public synchronized void close() {
		for(Closeable closeable : this.closeables) {
			IOUtils.closeQuietly(closeable);
		}
		isClosed = true;
	}
	
	private void checkClosed() {
		if (isClosed) {
			log.warn("Attempting operation on a closed bag. The results may be problematic.");
		}
	}
}