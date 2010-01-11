package gov.loc.repository.bagit.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.exec.OS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class AddFilesToPayloadOperation extends LongRunningOperationBase {

	private static final Log log = LogFactory.getLog(AddFilesToPayloadOperation.class);
	private Bag bag;
	
	public AddFilesToPayloadOperation(Bag bag){
		this.bag = bag;		
	}
	
	public Bag addFilesToPayload(List<File> files) {
		int total = files.size();
		int count = 0;
			
		for(File file : files) {
			if (this.isCancelled()) return null;
			String filepath = file.getAbsolutePath();
			count++;
			this.progress("Adding payload file to data directory", filepath, count, total);
			log.trace(MessageFormat.format("Adding payload {0} in data directory", filepath));
			this.addFileToPayload(file);
		}
		return this.bag;
	}

	public void addFileToPayload(File file) {
		if (! file.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist.", file));
		}
		if (! file.canRead()) {
			throw new RuntimeException(MessageFormat.format("Can't read {0}.", file));
		}
		this.addPayload(file, file.getParentFile());
	}

	private void addPayload(File file, File rootDir) {
			if (! file.canRead()) {
				throw new RuntimeException("Can't read " + file.toString());
			}
			//If directory, recurse on children
			if (file.isDirectory()) {
				int total = file.listFiles().length;
				int count = 0;
				
				for(File child : file.listFiles()) {
					//TODO
//					if (this.isCancelled()) return null;
					String filepath = file.getAbsolutePath();
					count++;
					this.progress("Adding payload file to data directory", filepath, count, total);
					log.trace(MessageFormat.format("Adding payload {0} in data directory", filepath));
					this.addPayload(child, rootDir);
				}
					
			} else if (file.isFile()) {
				
				//If file, add to payloadMap
				String filepath = this.bag.getBagConstants().getDataDirectory() + "/";
				if (rootDir != null) {
					filepath += FilenameHelper.removeBasePath(rootDir.toString(), file.toString());
				} else {
					filepath += file.toString();
				}
				//TODO
				if ((filepath.indexOf('\\') != -1)&&(OS.isFamilyWindows()))  {
					throw new UnsupportedOperationException("This Library does not support \\ in filepaths: " + filepath);
				}
				
				log.debug(MessageFormat.format("Adding {0} to payload.", filepath));
				this.bag.putBagFile(new FileBagFile(filepath, file));
			}
			else {
				throw new RuntimeException("Neither a directory or file");
			}

		}
	
}
