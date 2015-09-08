package gov.loc.repository.bagit;

import java.text.MessageFormat;
import java.util.List;

public interface FetchTxt extends List<FetchTxt.FilenameSizeUrl>, BagFile {

	static final String NO_SIZE_MARKER = "-";

	public enum FetchStatus {
		NOT_FETCHED, 
		FETCH_FAILED, 
		VERIFY_FAILED, 
		SUCCEEDED;
	}

	public class FilenameSizeUrl {
		private String filename;
		private Long size;
		private String url;
		private FetchStatus fetchStatus;

		/**
		 * Default constructor.
		 */
		public FilenameSizeUrl() {
		}

		/**
		 * Constructor that sets the filename, size, and url.
		 * @param filename The filename of the payload file.
		 * @param size The number of octets in the file.
		 * @param url The location of the file to be fetched.
		 */
		public FilenameSizeUrl(String filename, Long size, String url) {
			this.setFilename(filename);
			this.setSize(size);
			this.setUrl(url);
		}
		
		/**
		 * Constructor that sets the filename, size, url, and fetchstatus.
		 * @param filename The filename of the payload file.
		 * @param size The number of octets in the file.
		 * @param url The location of the file to be fetched.
		 * @param fetchStatus The status of the file being fetched.
		 */
		public FilenameSizeUrl(String filename, Long size, String url, FetchStatus fetchStatus) {
			this.setFilename(filename);
			this.setSize(size);
			this.setUrl(url);
			this.setFetchStatus(fetchStatus);
		}
		
		/**
		 * Sets the filename of the payload file.
		 * @param filename The filename of the payload file.
		 */
		public void setFilename(String filename) {
			this.filename = filename;
		}
		
		/**
		 * Gets the filename of the payload file.
		 * @return The filename of the payload file.
		 */
		public String getFilename() {
			return filename;
		}
		
		/**
		 * Sets the number of octets in the file.
		 * @param size The number of octets in the file.
		 */
		public void setSize(Long size) {
			this.size = size;
		}
		
		/**
		 * Gets the number of octets in the file.
		 * @return The number of octets in the file.
		 */
		public Long getSize() {
			return size;
		}
		
		/**
		 * Sets the location of the file to be fetched.
		 * @param url The location of the file to be fetched.
		 */
		public void setUrl(String url) {
			this.url = url;
		}
		
		/**
		 * Gets the location of the file to be fetched.
		 * @return The location of the file to be fetched.
		 */
		public String getUrl() {
			return url;
		}
		
		/**
		 * Gets the FetchStatus of the file that is being fetched.
		 * @return The status of a file being fetched.
		 */
		public FetchStatus getFetchStatus(){
			return this.fetchStatus;
		}
		
		/**
		 * Sets the FetchStatus of a file that is being fetched.
		 * @param fetchStatus The status of a file being fetched.
		 */
		public void setFetchStatus(FetchStatus fetchStatus){
			this.fetchStatus = fetchStatus;
		}
		
		@Override
		public String toString() {
			String size = NO_SIZE_MARKER;
			if (this.size != null) {
				size = this.size.toString();
			}
			return MessageFormat.format("Filename is {0}. Size is {1}. Url is {2}. Fetch status is {3}.", this.filename, size, this.url, this.fetchStatus);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((filename == null) ? 0 : filename.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FilenameSizeUrl other = (FilenameSizeUrl) obj;
			if (filename == null) {
				if (other.filename != null)
					return false;
			} else if (!filename.equals(other.filename))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}
	}
}
