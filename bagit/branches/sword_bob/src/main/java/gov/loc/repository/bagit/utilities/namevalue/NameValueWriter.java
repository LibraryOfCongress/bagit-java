package gov.loc.repository.bagit.utilities.namevalue;

public interface NameValueWriter {
	void close();
	void write(String name, String value);
}
