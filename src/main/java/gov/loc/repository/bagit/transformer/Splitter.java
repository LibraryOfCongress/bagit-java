package gov.loc.repository.bagit.transformer;

import gov.loc.repository.bagit.Bag;

import java.util.List;

public interface Splitter {
	public static String FILE_TYPE_KEY = "File-Type";

	List<Bag> split(Bag bag);
}
