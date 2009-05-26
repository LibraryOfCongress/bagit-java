package gov.loc.repository.bagit.verify.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;

public class ValidVerifierImpl extends LongRunningOperationBase implements ValidVerifier {

	private static final Log log = LogFactory.getLog(ValidVerifierImpl.class);
	
	private CompleteVerifier completeVerifier;
	private ManifestChecksumVerifier manifestVerifier;
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		super.setCancelIndicator(cancelIndicator);
		
		if (completeVerifier instanceof Cancellable) {
			((Cancellable)completeVerifier).setCancelIndicator(cancelIndicator);
		}
		if (manifestVerifier instanceof Cancellable) {
			((Cancellable)manifestVerifier).setCancelIndicator(cancelIndicator);
		}		
	}

	@Override
	public void addProgressListener(ProgressListener progressListener) {
		super.addProgressListener(progressListener);
		
		if (completeVerifier instanceof ProgressListenable) {
			((ProgressListenable)completeVerifier).addProgressListener(progressListener);
		}
		if (manifestVerifier instanceof ProgressListenable) {
			((ProgressListenable)manifestVerifier).addProgressListener(progressListener);
		}
	}
	
	public ValidVerifierImpl(CompleteVerifier completeVerifier, ManifestChecksumVerifier manifestVerifier) {
		this.completeVerifier = completeVerifier;
		this.manifestVerifier = manifestVerifier;
	}
	
	@Override
	public SimpleResult verify(Bag bag) {
		//Is complete
		SimpleResult result = this.completeVerifier.verify(bag);
		if (this.isCancelled()) return null;
		if (! result.isSuccess())
		{
			return result;
		}

		//Every checksum checks
		result = this.manifestVerifier.verify(bag.getTagManifests(), bag);
		if (this.isCancelled()) return null;
		if (! result.isSuccess()) {
			return result;
		}

		result = this.manifestVerifier.verify(bag.getPayloadManifests(), bag);
		if (this.isCancelled()) return null;
		if (! result.isSuccess()) {
			return result;
		}
		
		log.info("Validity check: " + result.toString());				
		return result;

	}

}
