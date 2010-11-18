package gov.loc.repository.bagit;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.impl.BagItTxtImpl;
import gov.loc.repository.bagit.impl.VFSBagFile;
import gov.loc.repository.bagit.utilities.FormatHelper;
import gov.loc.repository.bagit.utilities.SizeHelper;
import gov.loc.repository.bagit.utilities.VFSHelper;
import gov.loc.repository.bagit.v0_95.impl.BagConstantsImpl;

public class BagHelper {
	
	private static final Log log = LogFactory.getLog(BagHelper.class);
	
	private static final String BAGIT = "bagit.txt";
	
	public static String getVersion(File bagFile) {
		FileObject fileObject = VFSHelper.getFileObject(bagFile);
		try {
		
			if (fileObject == null || ! fileObject.exists()) {
				log.debug(MessageFormat.format("Unable to determine version for {0}.", bagFile.toString()));
				return null;
			}
		
			//If a serialized bag, then need to get bag directory from within		
			if (FormatHelper.getFormat(bagFile).isSerialized) {
				if (fileObject.getChildren().length != 1) {
					throw new RuntimeException("Unable to find bag_dir in serialized bag");
				}
				fileObject = fileObject.getChildren()[0];
			}
			//Look for BagInfo.txt
			FileObject bagItFileObject = fileObject.getChild(BAGIT);
			if (bagItFileObject == null || ! bagItFileObject.exists()) {
				log.debug(MessageFormat.format("Unable to determine version for {0}.", bagFile.toString()));
				return null;
			}
			BagItTxt bagItTxt = new BagItTxtImpl(new VFSBagFile(BAGIT, bagItFileObject), new BagConstantsImpl());
			log.debug(MessageFormat.format("Determined that version for {0} is {1}.", bagFile.toString(), bagItTxt.getVersion()));
			return bagItTxt.getVersion();
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static long generatePayloadOctetCount(Bag bag) {
		long count = 0;
		for(BagFile bagFile : bag.getPayload()) {
			count = count + bagFile.getSize();
		}
		return count;
	}
	
	public static String generatePayloadOxum(Bag bag) {
		return Long.toString(generatePayloadOctetCount(bag)) + "." + Long.toString(bag.getPayload().size());
	}
	
	public static long generateTagOctetCount(Bag bag) {
		long count = 0;
		for(BagFile bagFile : bag.getTags()) {
			count = count + bagFile.getSize();
		}
		return count;
	}
	
	public static String generateBagSize(Bag bag) {
		long count = generateTagOctetCount(bag) + generatePayloadOctetCount(bag);
		return SizeHelper.getSize(count);
	}
	
	public static boolean isPayload(String filepath, BagConstants bagConstants) {
		if (filepath.startsWith(bagConstants.getDataDirectory())) {
			return true;
		}
		return false;
	}
}
