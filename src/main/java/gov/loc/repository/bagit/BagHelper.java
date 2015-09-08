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
	
	/**
	 * Returns format or null if unable to determine format.
	 * @param bagFile The File containing the bag.
	 * @return The version of the bag in String format.
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
	
	/**
	 * Returns the octet count of payload files.
	 * @param bag The bag whose payload file sizes to add.
	 * @return The octet count of payload files.
	 */
	public static long generatePayloadOctetCount(Bag bag) {
		log.debug("Generating payload octet count");
		long count = 0;
		for(BagFile bagFile : bag.getPayload()) {
			count = count + bagFile.getSize();
			log.trace(MessageFormat.format("Octet count after adding {0} is {1}", bagFile.getFilepath(), count));
		}
		return count;
	}
	
	/**
	 * Returns the octet count of payload files in String format.
	 * @param bag The bag whose payload file sizes to add.
	 * @return The octet count of payload files in String format.
	 */
	public static String generatePayloadOxum(Bag bag) {
		return Long.toString(generatePayloadOctetCount(bag)) + "." + Long.toString(bag.getPayload().size());
	}
	
	/**
	 * Returns the octet count of tag files.
	 * @param bag The bag whose tag file sizes to add.
	 * @return The octet count of tag files.
	 */
	public static long generateTagOctetCount(Bag bag) {
		log.debug("Generating tag octet count");
		long count = 0;
		for(BagFile bagFile : bag.getTags()) {			
			count = count + bagFile.getSize();
			log.trace(MessageFormat.format("Octet count after adding {0} is {1}", bagFile.getFilepath(), count));
		}
		return count;
	}
	
	/**
	 * Returns the size of all payload and tag files.
	 * @param bag The bag whose payload and tag file sizes to add.
	 * @return The size of all payload and tag files.
	 */
	public static String generateBagSize(Bag bag) {
		long count = generateTagOctetCount(bag) + generatePayloadOctetCount(bag);
		log.trace(MessageFormat.format("Total octet count is {0}", count));
		String size = SizeHelper.getSize(count);
		log.trace(MessageFormat.format("Size is {0}", size));
		return size;
	}
	
	/**
	 * Returns True if the file is a payload file, false otherwise.
	 * @param filepath The location of the file.
	 * @param bagConstants The names of constants associated with a bag.
	 * @return True if the file is a payload file, false otherwise.
	 */
	public static boolean isPayload(String filepath, BagConstants bagConstants) {
		filepath = FilenameUtils.normalize(filepath);
		return filepath.startsWith(bagConstants.getDataDirectory()); 
	}
	
}