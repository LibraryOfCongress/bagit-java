package gov.loc.repository.bagit;

import java.text.MessageFormat;
import java.util.List;

public interface FetchTxt extends List<FetchTxt.FilenameSizeUrl>, BagFile {

	static final String NO_SIZE_MARKER = "-";
	
	public class FilenameSizeUrl {
		private String filename;
		private Long size;
		private String url;
		
		public FilenameSizeUrl() {
		}

		public FilenameSizeUrl(String filename, Long size, String url) {
			this.setFilename(filename);
			this.setSize(size);
			this.setUrl(url);
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

		@Override
		public String toString() {
			String size = NO_SIZE_MARKER;
			if (this.size != null) {
				size = this.size.toString();
			}
			return MessageFormat.format("Filename is {0}. Size is {1}. Url is {2}.", this.filename, size, this.url);
		}
	}	

}
