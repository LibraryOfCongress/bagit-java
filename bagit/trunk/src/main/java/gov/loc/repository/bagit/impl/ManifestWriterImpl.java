package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.ManifestWriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManifestWriterImpl implements ManifestWriter {
	
	private static final Log log = LogFactory.getLog(ManifestWriterImpl.class);	
	
	private PrintWriter writer = null;
	private String separator = null;
	
	public ManifestWriterImpl(OutputStream out, String separator) {
		this.writer = new PrintWriter(out);
		this.separator = separator;
	}
			
	public void write(String file, String fixityValue) {
		this.writer.println(fixityValue + separator + file);
		log.debug(MessageFormat.format("Wrote to manifest:  Filename is {0}.  Fixity is {1}.", file, fixityValue));		
	}
	
	public void close() {
		this.writer.close();
	}
}
