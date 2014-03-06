package gov.loc.repository.bagit;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystemFactory;
import gov.loc.repository.bagit.filesystem.FileSystemFactory.UnsupportedFormatException;
import gov.loc.repository.bagit.impl.BagItTxtImpl;
import gov.loc.repository.bagit.impl.FileSystemBagFile;
import gov.loc.repository.bagit.utilities.FormatHelper.UnknownFormatException;
import gov.loc.repository.bagit.utilities.SizeHelper;
import gov.loc.repository.bagit.v0_95.impl.BagConstantsImpl;

public class BagHelper {
	
	private static final Log log = LogFactory.getLog(BagHelper.class);
	
	private static final String BAGIT = "bagit.txt";
	
	/*
	 * Returns format or null if unable to determine format.
	 */
	public static String getVersion(File bagFile) {
		DirNode bagFileDirNode = null;
		try {
			bagFileDirNode = FileSystemFactory.getDirNodeForBag(bagFile);
		} catch (UnknownFormatException e) {
			log.debug(MessageFormat.format("Unable to determine version for {0} because unknown format.", bagFile.toString()));
			return null;
		} catch (UnsupportedFormatException e) {
			log.debug(MessageFormat.format("Unable to determine version for {0} because unsupported format.", bagFile.toString()));
		}
		log.trace(MessageFormat.format("BagFileDirNode has filepath {0} and is a {1}", bagFileDirNode.getFilepath(), bagFileDirNode.getClass().getSimpleName()));
		
		FileNode bagItNode = bagFileDirNode.childFile(BAGIT);
		if (bagItNode == null || ! bagItNode.exists()) {
			log.debug(MessageFormat.format("Unable to determine version for {0}.", bagFile.toString()));
			return null;
		}
		try {
            BagItTxt bagItTxt = new BagItTxtImpl(new FileSystemBagFile(BAGIT, bagItNode), new BagConstantsImpl());
            log.debug(MessageFormat.format("Determined that version for {0} is {1}.", bagFile.toString(), bagItTxt.getVersion()));
            return bagItTxt.getVersion();
        } finally {
            bagFileDirNode.getFileSystem().closeQuietly();
        }
		
	}
	
	public static long generatePayloadOctetCount(Bag bag) {
		log.debug("Generating payload octet count");
		long count = 0;
		for(BagFile bagFile : bag.getPayload()) {
			count = count + bagFile.getSize();
			log.trace(MessageFormat.format("Octet count after adding {0} is {1}", bagFile.getFilepath(), count));
		}
		return count;
	}
	
	public static String generatePayloadOxum(Bag bag) {
		return Long.toString(generatePayloadOctetCount(bag)) + "." + Long.toString(bag.getPayload().size());
	}
	
	public static long generateTagOctetCount(Bag bag) {
		log.debug("Generating tag octet count");
		long count = 0;
		for(BagFile bagFile : bag.getTags()) {			
			count = count + bagFile.getSize();
			log.trace(MessageFormat.format("Octet count after adding {0} is {1}", bagFile.getFilepath(), count));
		}
		return count;
	}
	
	public static String generateBagSize(Bag bag) {
		long count = generateTagOctetCount(bag) + generatePayloadOctetCount(bag);
		log.trace(MessageFormat.format("Total octet count is {0}", count));
		String size = SizeHelper.getSize(count);
		log.trace(MessageFormat.format("Size is {0}", size));
		return size;
	}
	
	public static boolean isPayload(String filepath, BagConstants bagConstants) {
		filepath = FilenameUtils.normalize(filepath);
		return filepath.startsWith(bagConstants.getDataDirectory()); 
	}
	
	
}
