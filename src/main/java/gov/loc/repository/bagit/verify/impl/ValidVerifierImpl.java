package gov.loc.repository.bagit.verify.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.utilities.BagVerifyResult;
import gov.loc.repository.bagit.utilities.CancelUtil;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;

public class ValidVerifierImpl extends LongRunningOperationBase implements ValidVerifier {

	private static final Log log = LogFactory.getLog(ValidVerifierImpl.class);
	
	private CompleteVerifier completeVerifier;
	private ManifestChecksumVerifier manifestVerifier;
	
	public ValidVerifierImpl(CompleteVerifier completeVerifier, ManifestChecksumVerifier manifestVerifier) {
		this.completeVerifier = completeVerifier;
		this.manifestVerifier = manifestVerifier;
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
	
	@Override
	public void cancel()
	{
		super.cancel();
		
		CancelUtil.cancel(this.completeVerifier);
		CancelUtil.cancel(this.manifestVerifier);
	}
	
	//Return right away if a step fails.
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
	
	//Execute all the steps and merge the results, even if an earlier step fails.
	public BagVerifyResult verifyFailSlow(Bag bag) {
		
		if (bag.getPayloadManifests().isEmpty()) {
			String msg = "Bag does not have any payload manifests.";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		
		//Is complete
		BagVerifyResult result = (BagVerifyResult)this.completeVerifier.verify(bag);
		if (this.isCancelled()) return null;

		//Every checksum checks
		result.merge((BagVerifyResult)this.manifestVerifier.verify(bag.getTagManifests(), bag));
		if (this.isCancelled()) return null;

		result.merge((BagVerifyResult)this.manifestVerifier.verify(bag.getPayloadManifests(), bag));
		if (this.isCancelled()) return null;
		
		log.info("Validity check: " + result.toString());				
		return result;

	}

}
