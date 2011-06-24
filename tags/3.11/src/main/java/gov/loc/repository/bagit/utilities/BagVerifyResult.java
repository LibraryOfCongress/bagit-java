package gov.loc.repository.bagit.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BagVerifyResult extends SimpleResult {
    private Set<String> missingAndInvalidFiles = new HashSet<String>(); 
	
    public void addMissingOrInvalidFile(String filePath){
    	missingAndInvalidFiles.add(filePath);
    }
    
    public Set<String> getMissingAndInvalidFiles(){
    	return Collections.unmodifiableSet(this.missingAndInvalidFiles);
    }
    
	public BagVerifyResult(boolean isSuccess) {
		super(isSuccess);
	}
	
	public BagVerifyResult(boolean isSuccess, String message) {
		super(isSuccess, message);
	}
	
	@Override
	public void merge(SimpleResult result){
		if(result == null) {
			return;
		}
		super.merge(result);
		if(result instanceof BagVerifyResult) {
			this.missingAndInvalidFiles.addAll(((BagVerifyResult) result).getMissingAndInvalidFiles());
		}
	}

	@Override
	public String toString() {
		return super.toString() + " missingAndInvalidFiles: " + this.missingAndInvalidFiles.toString();
	}
}
