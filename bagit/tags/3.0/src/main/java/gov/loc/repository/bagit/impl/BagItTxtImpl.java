package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.utilities.namevalue.impl.AbstractNameValueBagFile;

public class BagItTxtImpl extends AbstractNameValueBagFile implements BagItTxt {

	public static final String VERSION_KEY = "BagIt-Version";
	public static final String CHARACTER_ENCODING_KEY = "Tag-File-Character-Encoding";	
	
	private static final long serialVersionUID = 1L;

	public BagItTxtImpl(BagFile bagFile, BagConstants bagConstants) {
		super(bagConstants.getBagItTxt(), bagFile, bagConstants.getBagEncoding());
	}
	
	public BagItTxtImpl(BagConstants bagConstants) {
		super(bagConstants.getBagItTxt(), bagConstants.getBagEncoding());
		this.put(VERSION_KEY, bagConstants.getVersion().versionString);
		this.put(CHARACTER_ENCODING_KEY, bagConstants.getBagEncoding());
			
	}
	
	public String getCharacterEncoding() {
		return this.get(CHARACTER_ENCODING_KEY);
	}

	public String getVersion() {
		return this.get(VERSION_KEY);
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
}
