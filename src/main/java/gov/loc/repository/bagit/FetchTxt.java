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

		public FilenameSizeUrl() {
		}

		public FilenameSizeUrl(String filename, Long size, String url) {
			this.setFilename(filename);
			this.setSize(size);
			this.setUrl(url);
		}
		
		public FilenameSizeUrl(String filename, Long size, String url, FetchStatus fetchStatus) {
			this.setFilename(filename);
			this.setSize(size);
			this.setUrl(url);
			this.setFetchStatus(fetchStatus);
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getFilename() {
			return filename;
		}

		public void setSize(Long size) {
			this.size = size;
		}

		public Long getSize() {
			return size;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		public FetchStatus getFetchStatus(){
			return this.fetchStatus;
		}
		
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
