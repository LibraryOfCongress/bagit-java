package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.ManifestReader;
import gov.loc.repository.bagit.utilities.FilenameHelper;

import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManifestReaderImpl implements ManifestReader {

	private static final Log log = LogFactory.getLog(ManifestReaderImpl.class);
	
	private BufferedReader reader = null;
	private FilenameFixity next = null;
	private String splitRegex = null;
	private boolean treatBackwardSlashAsPathSeparator = false;
	
	public ManifestReaderImpl(InputStream in, String encoding, String splitRegex, boolean treatBackwardSlashAsPathSeparator) {
		this.splitRegex = splitRegex;
		this.treatBackwardSlashAsPathSeparator = treatBackwardSlashAsPathSeparator;
		try
		{
			// Replaced FileReader with InputStreamReader since all bagit manifest and metadata files must be UTF-8
			// encoded.  If UTF-8 is not explicitly set then data will be read in default native encoding.
			InputStreamReader fr = new InputStreamReader(in, encoding);
			this.reader = new BufferedReader(fr);
			this.setNext();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
		
	public boolean hasNext() {
		if (this.next == null)
		{
			return false;
		}
		return true;
	}	
	
	private void setNext()
	{
		try
		{
			while(true)
			{
				String line = this.reader.readLine();
				log.trace("Line: " + line);
				if (line == null)
				{
					this.next = null;
					return;
				}				
				String[] splitString = line.split(this.splitRegex, 2);
				if (splitString.length == 2)
				{
					String filepath = splitString[1];
					log.trace("Filepath before normalization: " + filepath);
					if (this.treatBackwardSlashAsPathSeparator) {
						filepath = FilenameHelper.normalizePathSeparators(filepath);
					} else if (filepath.indexOf('\\') != -1) {
						throw new UnsupportedOperationException(MessageFormat.format("This Library does not support \\ in filepaths: {0}. See README.txt.", filepath));
					}
					filepath = FilenameHelper.normalizePath(filepath);
					log.trace("Filepath after normalization: " + filepath);
					this.next = new FilenameFixity(filepath, splitString[0]);
					log.debug("Read: " + this.next);
					return;
				}						
				
			}
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
				
	}
	
	public FilenameFixity next() {
		if (this.next == null)
		{
			throw new NoSuchElementException();
		}
		FilenameFixity returnFileFixity = this.next;
		this.setNext();
		log.debug("Read from manifest: " + returnFileFixity.toString());
		return returnFileFixity;
		
	}
	
	public void remove() {
		throw new UnsupportedOperationException();		
	}	
	
	public void close()
	{
		try
		{
			if (this.reader != null)
			{
				this.reader.close();
			}
		}
		catch(IOException ex)
		{
			log.error(ex);
		}
	}
	
}
