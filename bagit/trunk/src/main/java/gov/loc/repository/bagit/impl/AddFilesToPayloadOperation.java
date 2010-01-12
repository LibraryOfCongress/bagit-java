package gov.loc.repository.bagit.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

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
	
	public void addFilesToPayload(List<File> files) {			
		int count = 0;
		for(File file : files) {
			if (this.isCancelled()) return;
			count = this.addPayload(file, file.getParentFile(), count);
		}
		return;
	}

	public void addFileToPayload(File file) {
		if (! file.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist.", file));
		}
		this.addPayload(file, file.getParentFile(), 0);
	}

	private int addPayload(File file, File rootDir, int count) {
		if (this.isCancelled()) return 0;
		if (! file.canRead()) {
			throw new RuntimeException("Can't read " + file.toString());
		}
		//If directory, recurse on children
		if (file.isDirectory()) {			
			for(File child : file.listFiles()) {
				if (this.isCancelled()) return 0;
				String filepath = file.getAbsolutePath();
				this.progress("Adding payload file to data directory", filepath, count, null);
				log.trace(MessageFormat.format("Adding payload {0} in data directory", filepath));
				count = this.addPayload(child, rootDir, count);
			}
					
		} else if (file.isFile()) {
			
			//If file, add to payloadMap
			String filepath = this.bag.getBagConstants().getDataDirectory() + "/";
			if (rootDir != null) {
				filepath += FilenameHelper.removeBasePath(rootDir.toString(), file.toString());
			} else {
				filepath += file.toString();
			}
			if (filepath.indexOf('\\') != -1)  {
				throw new UnsupportedOperationException(MessageFormat.format("This Library does not support \\ in filepaths: {0}. See README.txt.", filepath));
			}
			count++;				
			log.debug(MessageFormat.format("Adding {0} to payload.", filepath));
			this.bag.putBagFile(new FileBagFile(filepath, file));
		}
		else {
			throw new RuntimeException("Neither a directory or file");
		}
		return count;
	}
	
}
