package gov.loc.repository.bagit;

import java.io.Closeable;

public interface DeclareCloseable {
	
	/**
	 * Declares a BagFile as a Closeable object.
	 * @return A Closeable object.
	 */
	Closeable declareCloseable();

}