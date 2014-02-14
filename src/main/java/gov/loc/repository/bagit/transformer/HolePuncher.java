package gov.loc.repository.bagit.transformer;

import gov.loc.repository.bagit.Bag;

public interface HolePuncher {
	/**
	 * Make the bag holey.
	 * The involves creating a fetch.txt and removing the payload
	 * @param	bag the bag to make holey 
	 * @param	baseUrl	the url part to prepend to create the payload url
	 * @param	includePayloadDirectoryInUrl whether to include the payload directory ("data") in the payload url
	 * @param	includeTags whether to include the tags in the fetch.txt.  If true then includePayloadDirectory will be true.
	 * @param	leaveTags whether to leave the tags in the returned bag.
	 * @param	resume Whether to resume the process if it has already been started.
	 * @return	the newly holey bag
	 */
	Bag makeHoley(Bag bag, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean leaveTags, boolean resume);
	
	/**
	 * Make the bag holey.
	 * The involves creating a fetch.txt and removing the payload
	 * @param	bag the bag to make holey 
	 * @param	baseUrl	the url part to prepend to create the payload url
	 * @param	includePayloadDirectoryInUrl whether to include the payload directory ("data") in the payload url
	 * @param	includeTags whether to include the tags in the fetch.txt.  If true then includePayloadDirectory will be true.
	 * @param	resume Whether to resume the process if it has already been started.
	 * @return	the newly holey bag
	 */
	Bag makeHoley(Bag bag, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean resume);

}
