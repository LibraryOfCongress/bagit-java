package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.Bag.Format;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

public class FormatHelper {
	
	private static final String[] TAR_MAGIC_NUMBER = new String[] {"75", "73", "74", "61", "72"};
	private static final int TAR_OFFSET = 257;
	
	public static boolean isZip(File file) {
		return hasMagicNumber(file, new String[] {"50","4B"}, 0);
	}

	public static boolean isTar(File file) {
		return hasMagicNumber(file, TAR_MAGIC_NUMBER, TAR_OFFSET);
	}
	
	public static boolean isTarGz(File file) {
		if (! hasMagicNumber(file, new String[] {"1F", "8B"}, 0)) {
			return false;
		}
		try {
			return hasMagicNumber(new GZIPInputStream(new FileInputStream(file)), TAR_MAGIC_NUMBER, TAR_OFFSET);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static boolean isTarBz2(File file) {
		if (! hasMagicNumber(file, new String[] {"42", "5A"}, 0)) {
			return false;
		}
		try {
			InputStream in  = new FileInputStream(file);
			in.read();
			in.read();
			return hasMagicNumber( new CBZip2InputStream(in), TAR_MAGIC_NUMBER, TAR_OFFSET);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
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
		else if (isTarGz(file)) {
			return Format.TAR_GZ;
		}
		else if (isTarBz2(file)) {
			return Format.TAR_BZ2;
		}
		throw new RuntimeException("Unknown format");
	}

	
	
}
