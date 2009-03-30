package gov.loc.repository.bagit;

public interface BagItTxt extends BagFile {
	
	static final String TYPE = "BagItTxt";
	
	String getVersion();
	
	String getCharacterEncoding();
}
