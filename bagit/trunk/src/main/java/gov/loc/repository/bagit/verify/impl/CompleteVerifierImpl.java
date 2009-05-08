package gov.loc.repository.bagit.verify.impl;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ProgressIndicator;
import gov.loc.repository.bagit.ProgressMonitorable;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.VFSHelper;
import gov.loc.repository.bagit.verify.CompleteVerifier;

public class CompleteVerifierImpl implements CompleteVerifier, Cancellable, ProgressMonitorable {

	private static final Log log = LogFactory.getLog(CompleteVerifierImpl.class);
	
	private boolean missingBagItTolerant = false;
	private CancelIndicator cancelIndicator = null;
	private ProgressIndicator progressIndicator = null;
	
	public void setMissingBagItTolerant(boolean missingBagItTolerant) {
		this.missingBagItTolerant = missingBagItTolerant;
	}
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;
		
	}
	
	@Override
	public void setProgressIndicator(ProgressIndicator progressIndicator) {
		this.progressIndicator = progressIndicator;
	}
	
	@Override
	public SimpleResult verify(Bag bag) {
		SimpleResult result = new SimpleResult(true);
		try
		{
			//Is at least one payload manifest		
			if (bag.getPayloadManifests().isEmpty()) {
				result.setSuccess(false);
				result.addMessage("Bag does not have any payload manifests.");
			}
			//Has bagit file
			if (! missingBagItTolerant && bag.getBagItTxt() == null) {
				result.setSuccess(false);
				result.addMessage(MessageFormat.format("Bag does not have {0}.", bag.getBagConstants().getBagItTxt()));				
			}
			//Bagit is right version
			if (! missingBagItTolerant && bag.getBagItTxt() != null && ! bag.getBagConstants().getVersion().versionString.equals(bag.getBagItTxt().getVersion())) {
				result.setSuccess(false);
				result.addMessage(MessageFormat.format("Version is not {0}.", bag.getBagConstants().getVersion()));				
			}

			if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
			
			//All payload files are in data directory
			int total = bag.getPayload().size();
			int count = 0;
			for(BagFile bagFile : bag.getPayload()) {
				if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
				String filepath = bagFile.getFilepath();
				count++;
				if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying payload file in data directory", filepath, count, total);
				if (! filepath.startsWith(bag.getBagConstants().getDataDirectory() + '/')) {
					result.setSuccess(false);
					result.addMessage(MessageFormat.format("Payload file {0} not in the {1} directory", filepath, bag.getBagConstants().getDataDirectory()));									
				}
			}
			//Every payload BagFile in at least one manifest
			total = bag.getPayload().size();
			count = 0;
			for(BagFile bagFile : bag.getPayload()) {
				String filepath = bagFile.getFilepath();
				count++;
				if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying payload file in at least one manifest", filepath, count, total);
				boolean inManifest = false;
				for(Manifest manifest : bag.getPayloadManifests()) {
					if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
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
			
			//Every payload file exists
			total = bag.getPayloadManifests().size();
			count = 0;
			for(Manifest manifest : bag.getPayloadManifests()) {			
				count++;
				if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying payload files in manifest exist", manifest.getFilepath(), count, total);
				this.checkManifest(manifest, bag, result);
				if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
			}

			//Every tag file exists
			total = bag.getTagManifests().size();
			count = 0;
			for(Manifest manifest : bag.getTagManifests()) {
				count++;
				if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying tag files in manifest exist", manifest.getFilepath(), count, total);
				this.checkManifest(manifest, bag, result);
				if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
			}
			
			//Additional checks if an existing Bag
			if (bag.getFile() != null) {
				FileObject bagFileObject = VFSHelper.getFileObjectForBag(bag.getFile());
				//Only directory is a data directory
				for(FileObject fileObject : bagFileObject.getChildren())
				{
					if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
					if (fileObject.getType() == FileType.FOLDER) {
						String folderName = bagFileObject.getName().getRelativeName(fileObject.getName());
						if (! folderName.equals(bag.getBagConstants().getDataDirectory())) {
							result.setSuccess(false);
							result.addMessage(MessageFormat.format("Directory {0} not allowed in bag_dir", folderName));
						}
					}
				}
				//If there is a bagFileObject, all payload FileObjects have payload BagFiles
				FileObject dataFileObject = bagFileObject.getChild(bag.getBagConstants().getDataDirectory());
				if (dataFileObject != null) {
					FileObject[] fileObjects = bagFileObject.getChild(bag.getBagConstants().getDataDirectory()).findFiles(new FileTypeSelector(FileType.FILE));
					total = fileObjects.length;
					count = 0;
					for(FileObject fileObject : fileObjects) {
						if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
						String filepath = bagFileObject.getName().getRelativeName(fileObject.getName());
						count++;
						if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying payload files on disk are in bag", filepath, count, total);
						if (bag.getBagFile(filepath) == null) {
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

	protected void checkManifest(Manifest manifest, Bag bag, SimpleResult result) {
		int manifestTotal = manifest.keySet().size();
		int manifestCount = 0;
		for(String filepath : manifest.keySet()) {
			if (cancelIndicator != null && cancelIndicator.performCancel()) return;
			manifestCount++;
			if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying files in manifest exist", filepath, manifestCount, manifestTotal);
			BagFile bagFile = bag.getBagFile(filepath);					
			if (bagFile == null || ! bagFile.exists())
			{
				result.setSuccess(false);
				String message = MessageFormat.format("File {0} in manifest {1} missing from bag.", filepath, manifest.getFilepath());
				log.info(message);
				result.addMessage(message);
			}
		}				
		
	}

	
}
