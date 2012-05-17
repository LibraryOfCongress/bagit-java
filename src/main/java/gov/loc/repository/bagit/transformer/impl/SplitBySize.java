package gov.loc.repository.bagit.transformer.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.transformer.Splitter;
import gov.loc.repository.bagit.utilities.SizeHelper;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader.NameValue;

public class SplitBySize implements Splitter{

	private Double maxBagSize;
	private boolean keepLowestLevelDir;
	private BagFactory bagFactory;
	private String[] exludeDirs;

	public SplitBySize(BagFactory bagFactory, Double maxBagSize, boolean keepLowestLevelDir, String[] excludeDirs) {
		this.bagFactory = bagFactory;
		this.setKeepLowestLevelDir(keepLowestLevelDir);
		this.setMaxBagSize(maxBagSize);
		this.setExludeDirs(excludeDirs);
	}
	
	@Override
	public List<Bag> split(Bag srcBag) {
	    List<Bag> subBags = new ArrayList<Bag>();
		
		//Sort bag files in the source bag
		List<BagFile> sortedBagFiles = this.sortBagFiles(srcBag.getPayload(), this.isKeepLowestLevelDir(), this.getMaxBagSize(), this.getExludeDirs());
	    
	    //Group bag files of the source bag
	    List<BagFileGroup> bagFileGroups = group(sortedBagFiles, this.getMaxBagSize());
        
	    //Put each group of bag files to a separate new bag
	    for(BagFileGroup bagFileGroup : bagFileGroups) {
	    	List<BagFile> groupBagFiles = bagFileGroup.getBagFiles();
	    	Bag subBag = bagFactory.createBag(srcBag.getVersion());	    	
	    	
	    	if (srcBag.getBagInfoTxt() != null) {
		    	BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
		    	subBag.putBagFile(bagInfoTxt);
		    	//Add bag info from the source bag to the split bag
		    	List<NameValue> list = srcBag.getBagInfoTxt().asList();
		    	for(NameValue nameValue: list){
			    	subBag.getBagInfoTxt().put(nameValue);	    		
		    	}
	    	}
	    	
	    	for(BagFile bagFile : groupBagFiles){
	    		if(bagFile instanceof LowestLevelBagDir){
	    			subBag.putBagFiles(((LowestLevelBagDir) bagFile).getBagFiles());
	    		} else {
	    			subBag.putBagFile(bagFile);
	    		}
	    	}
	    	
	    	subBags.add(subBag);
	    }
		return subBags;
	}

	private List<BagFile> sortBagFiles(Collection<BagFile> payloadBagFiles, boolean keepLowestLevelDir, Double maxBagSize, String[] excludeDirs){
    	List<BagFile> sortedBagFiles = new ArrayList<BagFile>();
		
    	//Get all the file path directories 
    	Set<String> filePathDirs = new HashSet<String>();
    	for(BagFile bagFile : payloadBagFiles){
    		if(! SplitBagHelper.isExcluded(excludeDirs, this.getFilePathDir(bagFile.getFilepath()))) {
        		filePathDirs.add(this.getFilePathDir(bagFile.getFilepath()));	
    		}
    	}
    	    	
    	if(keepLowestLevelDir){
    		for(String filePathdir : filePathDirs) {
    			//If a lowest level directory, group all bag files under the directory into a single LowestLevelBagDir object.  Add the single object to the result list.
    			if(isLowestLevelDir(filePathdir, filePathDirs)){
    	    		LowestLevelBagDir lowestLevelBagDir = new LowestLevelBagDir(filePathdir);
    	        	for(BagFile bagFile : payloadBagFiles){
    	        		if(this.getFilePathDir(bagFile.getFilepath()).equals(filePathdir)) {
    	            		lowestLevelBagDir.addBagFile(bagFile);
    	        		}
    	        	}
    	        	
    	        	if(lowestLevelBagDir.getSize() >= maxBagSize) {
    		    		throw new RuntimeException(MessageFormat.format("The size of the lowest level directory {0} exceeds the maximum split bag size {1}.", lowestLevelBagDir.getFilepath(), SizeHelper.getSize((long)maxBagSize.longValue())));
    		    	}
    	        	
    	        	sortedBagFiles.add(lowestLevelBagDir);
    			}
    			//Otherwise, add all the bag files under the directory to the result list.
    			else{
    				for(BagFile bagFile : payloadBagFiles){
    	        		if(this.getFilePathDir(bagFile.getFilepath()).equals(filePathdir)) {
    	        			if(bagFile.getSize() >= maxBagSize) {
    	        				throw new RuntimeException(MessageFormat.format("The size of the file {0} exceeds the maximum split bag size {1}.", bagFile.getFilepath(),SizeHelper.getSize((long)(maxBagSize.longValue()))));    				
    	        	    	}
    	        			if( ! SplitBagHelper.isExcluded(excludeDirs, bagFile.getFilepath())){
        	        			sortedBagFiles.add(bagFile);    	        				
    	        			}
    	        		}
    	        	}
    			}
    		}
    	}else{
    		for(BagFile bagFile : payloadBagFiles){
        		if(bagFile.getSize() >= maxBagSize) {
        			throw new RuntimeException(MessageFormat.format("The size of the file {0} exceeds the maximum split bag size {1}.", bagFile.getFilepath(),SizeHelper.getSize((long)(maxBagSize.longValue()))));    				
        	    }
        		if( ! SplitBagHelper.isExcluded(excludeDirs, bagFile.getFilepath())){
        			sortedBagFiles.add(bagFile);    	        				
    			}  
        	}
    	}
    	
    	return sortedBagFiles;
    }
	
	private String getFilePathDir(String filePath){
		return filePath.substring(0, filePath.lastIndexOf('/'));
	}
	 
	private boolean isLowestLevelDir(String filePathDir, Set<String> filePathDirs){    	
	    for(String filePathDirItem : filePathDirs) {	    	
	    	if(filePathDirItem.equals(filePathDir)){
	    		continue;
	    	}
	    	if(filePathDirItem.indexOf(filePathDir) >= 0){
	    		return false;
	    	}
	    }
	    return true;
	 }
	 
	 private List<BagFileGroup> group(List<BagFile> bagFiles, Double maxBagSize){
	    	
	   	//Sort bag files by size in descending order
	   	Collections.sort(bagFiles, new BagFileSizeReverseComparator());
	    	
	   	List<BagFileGroup> bagFileGroups = new ArrayList<BagFileGroup>();
	   	for(BagFile bagFile : bagFiles) {
	   		if(bagFileGroups.isEmpty()){
	   			BagFileGroup group = new BagFileGroup(maxBagSize);
	   			group.addBagFile(bagFile);
	   			bagFileGroups.add(group);
	   		} else {
	   			boolean foundSpace = false;
	    			
	   			//Put the bag file in the first group which has enough space for the bag file 
	   			for(BagFileGroup bagFileGroup : bagFileGroups){
	   				if(bagFileGroup.hasSpace(bagFile)) {
	   					bagFileGroup.addBagFile(bagFile);
	   					foundSpace = true;
	   					break;
	   				}
	   			}
	   			
	   			//If the bag file does not find a group, put it in a new group
	   			if(!foundSpace){
	   				BagFileGroup group = new BagFileGroup(maxBagSize);
	       			group.addBagFile(bagFile);
	       			bagFileGroups.add(group);
	   			}
	   		}
	   	}
	    
	   	return bagFileGroups;
	 }
	 
	 private class BagFileSizeReverseComparator implements Comparator<BagFile>{
		@Override
		public int compare(BagFile bagFile1, BagFile bagFile2) {			
			return new Long(bagFile2.getSize()).compareTo(new Long(bagFile1.getSize()));
		}
	    	
	 }
	 
	 private class BagFileGroup{
         List<BagFile> bagFiles = new ArrayList<BagFile>();
         Double groupSize = 0.0;
		 Double maxGroupSize = 300 * SizeHelper.GB; 

		 public BagFileGroup(Double maxGroupSize){
			 this.maxGroupSize = maxGroupSize;
		 }
			 
        public List<BagFile> getBagFiles() {
			return bagFiles;
		}
		
		public boolean hasSpace(BagFile bagFile){
			if(groupSize + bagFile.getSize() > maxGroupSize) {
				return false;
			}
			return true;
		}
		
		public void addBagFile(BagFile bagFile) {
			this.bagFiles.add(bagFile);
			this.groupSize += bagFile.getSize();
		}  
    }

	 private class LowestLevelBagDir implements BagFile{
		private String filePath;
		private List<BagFile> bagFiles = new ArrayList<BagFile>();
				
		public LowestLevelBagDir(String filePath) {
			this.filePath = filePath;
		}
			
		public List<BagFile> getBagFiles(){
			return this.bagFiles;
		}
				
		@Override
		public boolean exists() {
			throw new RuntimeException("Operation not supported exception.");
		}

		@Override
		public String getFilepath() {
			return this.filePath;
		}

		@Override
		public long getSize() {
			long length = 0L;
			for(BagFile bagFile : bagFiles) {
				length += bagFile.getSize();
			}
		    return length;
		}

		@Override
		public InputStream newInputStream() {
			throw new RuntimeException("Operation not supported exception.");
		}
				
		public void addBagFile(BagFile bagFile) {
			this.bagFiles.add(bagFile);
		}
    }
	
	 public Double getMaxBagSize() {
		return maxBagSize;
	 }

	public void setMaxBagSize(Double maxBagSize) {
		this.maxBagSize = maxBagSize;
	}

	public boolean isKeepLowestLevelDir() {
		return keepLowestLevelDir;
	}

	public void setKeepLowestLevelDir(boolean keepLowestLevelDir) {
		this.keepLowestLevelDir = keepLowestLevelDir;
	}
	
	public String[] getExludeDirs() {
		return exludeDirs;
	}

	public void setExludeDirs(String[] exludeDirs) {
		this.exludeDirs = exludeDirs;
	}
}
