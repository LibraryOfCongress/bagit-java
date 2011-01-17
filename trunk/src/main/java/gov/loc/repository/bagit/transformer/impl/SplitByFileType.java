package gov.loc.repository.bagit.transformer.impl;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.transformer.Splitter;

import java.util.ArrayList;
import java.util.List;

public class SplitByFileType implements Splitter{
	
	private BagFactory bagFactory;
	private String[][] fileExtensions;
	private String[] exludeDirs;

	public String[][] getFileExtensions() {
		return fileExtensions;
	}

	public void setFileExtensions(String[][] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public SplitByFileType(BagFactory bagFactory, String[][] fileExtensions, String[] excludeDirs) {
		this.bagFactory = bagFactory;
		this.setFileExtensions(fileExtensions);
		this.setExludeDirs(excludeDirs);
	}
	
	@Override
	public List<Bag> split(Bag srcBag) {
	    List<Bag> subBags = new ArrayList<Bag>();

		for(String[] subFileExtension : this.fileExtensions){
			//Sort out targeted bag files in the source bag
		    List<BagFile> targetedBagFiles = new ArrayList<BagFile>();
		    for(BagFile bagFile : srcBag.getPayload()){
		    	String fileExtension = bagFile.getFilepath().substring(bagFile.getFilepath().lastIndexOf('.') + 1);
		    	for(String fileEx : subFileExtension){
		    		if(fileEx.trim().equalsIgnoreCase(fileExtension) && ! SplitBagHelper.isExcluded(this.getExludeDirs(), bagFile.getFilepath())){
		    			targetedBagFiles.add(bagFile);
		    			break;
		    		}
		    	}
		    }
		    
		    if(targetedBagFiles.size() > 0){
		    	//Put the targeted bag files to a new bag
			    Bag subBag = this.bagFactory.createBag(srcBag.getVersion());	    	
			    BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
			    subBag.putBagFile(bagInfoTxt);
			    //Add bag info from the source bag to the new bag
			    subBag.getBagInfoTxt().putAll(srcBag.getBagInfoTxt());
			    //Put file type info into bag-info.txt
			    subBag.getBagInfoTxt().put(FILE_TYPE_KEY, this.concatStrings(subFileExtension));
			    
			    subBag.putBagFiles(targetedBagFiles);			 
			    
			    subBags.add(subBag);
		    }
		}
	
	    return subBags;
	}
	
	private String concatStrings(String[] strs){
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(String str: strs) {
			if(i > 0) {
				sb.append(" ").append(str);
			}else{
				sb.append(str);				
			}
			i++;			
		}
		return sb.toString();
	}
	
	public String[] getExludeDirs() {
		return exludeDirs;
	}

	public void setExludeDirs(String[] exludeDirs) {
		this.exludeDirs = exludeDirs;
	}
}
