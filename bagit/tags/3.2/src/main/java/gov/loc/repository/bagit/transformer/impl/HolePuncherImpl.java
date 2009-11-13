package gov.loc.repository.bagit.transformer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.transformer.HolePuncher;

public class HolePuncherImpl extends AbstractBagVisitor implements HolePuncher {
	private static final Log log = LogFactory.getLog(HolePuncherImpl.class);
	private String baseUrl;
	private Bag newBag;
	private FetchTxt fetch;
	private boolean includePayloadDirectory = false;
	private boolean includeTags = false;
	private BagFactory bagFactory;
	private boolean leaveTags = true;
	
	public HolePuncherImpl(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}

	@Override
	public Bag makeHoley(Bag bag, String baseUrl, boolean includePayloadDirectoryInUrl,
			boolean includeTags) {
		return this.makeHoley(bag, baseUrl, includePayloadDirectoryInUrl, includeTags, true);
	}
	
	@Override
	public Bag makeHoley(Bag bag, String baseUrl, boolean includePayloadDirectoryInUrl,
			boolean includeTags, boolean leaveTags) {
		log.info("Making bag holey");
		this.baseUrl = baseUrl;
		if (! this.baseUrl.endsWith("/")) {
			this.baseUrl += "/";
		}
		this.includePayloadDirectory = includePayloadDirectoryInUrl;
		this.includeTags = includeTags;
		if (includeTags) {
			this.includePayloadDirectory = true;
		}
		this.leaveTags = leaveTags;
		bag.accept(this);
		return this.newBag;
	}

	@Override
	public void startBag(Bag bag) {
		this.newBag = this.bagFactory.createBag(bag.getBagConstants().getVersion());
		this.fetch = this.newBag.getBagPartFactory().createFetchTxt();
		this.newBag.putBagFile(this.fetch);

	}
	
	@Override
	public void visitPayload(BagFile bagFile) {
		String url = baseUrl;
		if (includePayloadDirectory) {
			url += bagFile.getFilepath();
		}
		else {
			url += bagFile.getFilepath().substring(this.newBag.getBagConstants().getDataDirectory().length() + 1);
		}
		fetch.add(new FetchTxt.FilenameSizeUrl(bagFile.getFilepath(), bagFile.exists()?bagFile.getSize():null, url));
	}
	
	@Override
	public void visitTag(BagFile bagFile) {
		if (includeTags) {
			String url = baseUrl + bagFile.getFilepath();
			fetch.add(new FetchTxt.FilenameSizeUrl(bagFile.getFilepath(), bagFile.exists()?bagFile.getSize():null, url));
		}
		if (! includeTags || leaveTags) {
			this.newBag.putBagFile(bagFile);
		}
	}		
}
