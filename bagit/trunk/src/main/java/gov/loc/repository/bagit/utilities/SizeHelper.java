package gov.loc.repository.bagit.utilities;

import java.text.DecimalFormat;

public class SizeHelper {

	public static final double KB = Math.pow(2, 10);
	public static final double MB = Math.pow(2, 20);
	public static final double GB = Math.pow(2, 30);
	public static final double TB = Math.pow(2, 40);
	
	public static String getSize(long octets) {
		String unit;
		double div;
		if (octets < MB) {
			//Return KB
			unit = "KB";
			div = KB;			
		} else if (octets < GB) {
			//Return MB
			unit = "MB";
			div = MB;
		} else if (octets < TB) {
			//Return GB
			unit = "GB";
			div = GB;
		} else {
			//Return TB
			unit = "TB";
			div = TB;
		}
		String format = "#.#";
		double size = octets/div;
		String sizeString = (new DecimalFormat(format)).format(size);
		while (sizeString.endsWith("0")) {
			format += "#";
			sizeString = (new DecimalFormat(format)).format(size);
		}
		return sizeString + " " + unit;
	}
	
}
