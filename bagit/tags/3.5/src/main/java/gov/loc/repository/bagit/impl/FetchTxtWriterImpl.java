package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxtWriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FetchTxtWriterImpl implements FetchTxtWriter {
	private static final Log log = LogFactory.getLog(FetchTxtWriterImpl.class);
	
	public static final String SEPARATOR = "  ";
	
	private PrintWriter writer = null;
	
	public FetchTxtWriterImpl(OutputStream out) {
		this.writer = new PrintWriter(out);
	}
	
	@Override
	public void write(String filename, Long size, String url) {
		String sizeString = FetchTxt.NO_SIZE_MARKER;
		if (size != null) {
			sizeString = size.toString();
		}
		try {
			String newUrl = url.replaceAll(" ", "%20");
			this.writer.println(newUrl + SEPARATOR + sizeString + SEPARATOR + filename);
			log.debug(MessageFormat.format("Wrote to fetch.txt:  Filename is {0}.  Size is {1}. Url is {2}.", filename, size, newUrl));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
		
	public void close() {
		this.writer.close();
	}
}
