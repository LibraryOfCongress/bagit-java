package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.Bag.Format;

import java.io.File;
import java.io.FileInputStream;

public class FormatHelper {
	
	public static boolean isZip(File file) {
		return hasMagicNumber(file, new String[] {"50","4B"}, 0);
	}

	public static boolean isTar(File file) {
		return hasMagicNumber(file, new String[] {"75", "73", "74", "61", "72"}, 257);
	}
	
	private static boolean hasMagicNumber(File file, String[] magicNumber, int offset) {
		boolean matches = true;		
		try {
			FileInputStream in = new FileInputStream(file);
			
			for(int i=0; i < offset; i++) {
				in.read();
			}
			
			for(String magicPart : magicNumber) {
				String filePart = Integer.toHexString(in.read());
				if (! filePart.equalsIgnoreCase(magicPart)) {
					matches = false;
				}
			}
					
			in.close();
			return matches;
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static Format getFormat(File file) {
		if (file == null) {
			throw new RuntimeException("Cannot determine format");
		}
		else if (file.isDirectory()) {
			return Format.FILESYSTEM;
		}
		else if (isZip(file)) {
			return Format.ZIP;
		}
		else if (isTar(file)) {
			return Format.TAR;
		}

		throw new RuntimeException("Unknown format");
	}

	
	
}
