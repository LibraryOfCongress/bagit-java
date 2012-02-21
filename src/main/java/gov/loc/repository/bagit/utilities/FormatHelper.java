package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.Bag.Format;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FormatHelper {
	
	public static boolean isZip(File file) {
		return hasMagicNumber(file, new String[] {"50","4B"}, 0);
	}
	
	private static boolean hasMagicNumber(File file, String[] magicNumber, int offset) {
		try {
			return hasMagicNumber(new FileInputStream(file), magicNumber, offset);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static boolean hasMagicNumber(InputStream in, String[] magicNumber, int offset) {
		boolean matches = true;		
		try {
			for(int i=0; i < offset; i++) {
				in.read();
			}
			
			for(String magicPart : magicNumber) {
				String filePart = Integer.toHexString(in.read());
				if (! filePart.equalsIgnoreCase(magicPart)) {
					matches = false;
				}
			}					
			return matches;
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	public static Format getFormat(File file) throws UnknownFormatException {
		if (file == null) {
			throw new RuntimeException("Cannot determine format");
		}
		else if (file.isDirectory()) {
			return Format.FILESYSTEM;
		}
		else if (isZip(file)) {
			return Format.ZIP;
		}
		throw new UnknownFormatException();
	}

	public static class UnknownFormatException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnknownFormatException() {
			super("Unknown format");
		}
	}
	
}
