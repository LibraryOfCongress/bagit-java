package gov.loc.repository.bagit;

import java.io.Closeable;
import java.text.MessageFormat;
import java.util.Iterator;

public interface ManifestReader extends Iterator<ManifestReader.FilenameFixity>, Closeable {

	public class FilenameFixity {
		private String filename;
		private String fixityValue;
		
		/**
		 * Constructor that sets the filename and fixityValue.
		 * @param file Name of the file.
		 * @param fixityValue A checksum algorithm hash function value.
		 */
		public FilenameFixity(String file, String fixityValue) {
			this.filename = file;
			this.fixityValue = fixityValue;
		}
			
		/**
		 * Default constructor.
		 */
		public FilenameFixity()	{			
		}
		
		/**
		 * Sets the filename.
		 * @param file Name of the file.
		 */
		public void setFilename(String file) {
			this.filename = file;
		}
		
		/**
		 * Gets the filename.
		 * @return The filename.
		 */
		public String getFilename() {
			return filename;
		}
		
		/**
		 * Sets the fixityValue of a file.
		 * @param fixityValue A checksum algorithm hash function value.
		 */
		public void setFixityValue(String fixityValue) {
			this.fixityValue = fixityValue;
		}
		
		/**
		 * Returns the fixityValue. 
		 * @return The fixityValue.
		 */
		public String getFixityValue() {
			return fixityValue;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("Filename is {0}.  Fixity is {1}.", this.filename, this.fixityValue);
		}
	}	
}
