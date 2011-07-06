package gov.loc.repository.bagit.transformer.impl;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class ChainingCompleter extends LongRunningOperationBase implements Completer {

	private Completer[] completers;
	
	public ChainingCompleter(Completer... completers) {
		this.completers = completers;
		for(Completer completer : completers) {
			if (completer instanceof Cancellable) this.addChainedCancellable((Cancellable)completer);
			if (completer instanceof ProgressListenable) this.addChainedProgressListenable((ProgressListenable)completer);
		}
	}
	
	@Override
	public Bag complete(Bag bag) {
		Bag newBag = bag;
		for(Completer completer : completers) {
			newBag = completer.complete(newBag);
		}
		return newBag;		
	}

}
