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
	public void write(String filename, Long size, String url, FetchTxt.FetchStatus fetchStatus) {
		String sizeString = FetchTxt.NO_SIZE_MARKER;
		if (size != null) {
			sizeString = size.toString();
		}
		try {
			String newUrl = url.replaceAll(" ", "%20");
			StringBuilder sb = new StringBuilder();
			sb.append(newUrl)
			  .append(SEPARATOR)
			  .append(sizeString);
			if(fetchStatus != null){
				sb.append(SEPARATOR)
				  .append(fetchStatus.toString());
			}
			sb.append(SEPARATOR)
			  .append(filename);
			  
			this.writer.println(sb.toString());
			log.debug(MessageFormat.format("Filename is {0}.  Size is {1}. Url is {2}.  Fetch status is {3}.", filename, size, newUrl, fetchStatus));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
		
	public void close() {
		this.writer.close();
	}
}
