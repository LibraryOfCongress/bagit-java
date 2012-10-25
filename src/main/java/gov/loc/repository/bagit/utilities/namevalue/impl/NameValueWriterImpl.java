package gov.loc.repository.bagit.utilities.namevalue.impl;

import gov.loc.repository.bagit.utilities.namevalue.NameValueWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NameValueWriterImpl implements NameValueWriter {	
	
	private static final Log log = LogFactory.getLog(NameValueWriterImpl.class);
	
	private PrintWriter writer = null;
	private int lineLength = 79;
	//Default to 4
	private String indent = "   ";
	private String type;
	Pattern lineTerminatorPattern = Pattern.compile("\r\n|\r|\n");
	
	public NameValueWriterImpl(OutputStream out, String encoding, int lineLength, int indentSpaces, String type) {
		this.init(out, encoding, type);
		this.lineLength = lineLength;
		this.indent = "";
		for(int i=0; i<indentSpaces; i++) {
			this.indent += " ";
		}
	}

	public NameValueWriterImpl(OutputStream out, String encoding, String type) {
		this.init(out, encoding, type);
	}
	
	private void init(OutputStream out, String encoding, String type) {
		try {
			this.writer = new PrintWriter(new OutputStreamWriter(out, encoding), true);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		this.type = type;
	}
	
	public void write(String name, String value) {		
		String line = name + ": " + (value !=null ? value : "");
		boolean isFirst = true;
		while (line.length() > 0) {
			int workingLength = lineLength;
			if (! isFirst) {
				workingLength = lineLength - this.indent.length();
			}
			String linePart = "";
			if (line.length() <= workingLength) {
				linePart = line;
				//Need to add newlines
				Matcher matcher = lineTerminatorPattern.matcher(linePart);
				linePart = matcher.replaceAll("\n" + indent + "\n" + indent);
				line = "";
			}
			else {
				//Look for newlines in substring(0, workinglength)
				Matcher matcher = lineTerminatorPattern.matcher(line);
				matcher.region(0,  workingLength);
				if (matcher.find()) {
					linePart = line.substring(0, matcher.start()) + "\n" + indent;
					
					line = line.substring(matcher.end());
				} else {
				
					//Start at lineLength and work backwards until find a space
					int index = workingLength;
					
					while(index >= 0 && line.charAt(index) != ' ') {
						index = index-1;
					}
					if (index < 0) {
						//Use whole line
						linePart = line;
						line = "";
					}
					else {
						linePart = line.substring(0, index);
						line = line.substring(index + 1);
					}
				}
			}
			if (isFirst) {
				isFirst = false;
			}
			else {
				linePart = this.indent + linePart;
			}
			this.writer.println(linePart);
		}
		log.debug(MessageFormat.format("Wrote to {0}: Name is {1}. Value is {2}.", this.type, name, value));
	}
	
	
	public void close() {
		this.writer.close();
	}
}
