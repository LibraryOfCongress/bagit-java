package gov.loc.repository.bagit.transformer.impl;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.SizeHelper;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BagSplitter {
	
    private static final Log log = LogFactory.getLog(BagSplitter.class);

    public SimpleResult splitBagBySize(File sourceBagFile, File destBagFileIn, Double maxBagSizeInGBIn)
	{		
    	SimpleResult splitBagResult = new SimpleResult(true);
		
    	//Load source bag
    	BagFactory bagFactory = new BagFactory();
		Bag sourceBag = bagFactory.createBag(sourceBagFile, BagFactory.LoadOption.BY_PAYLOAD_FILES);
		Double sourceBagSize = new Double(sourceBag.getBagInfoTxt().getPayloadOxum());

		//The default dest of split bags is parentDirOfSourceBag/SourceBagName_split
    	File destBagFile = destBagFileIn == null ? new File(sourceBagFile.getAbsoluteFile() + "_split") : destBagFileIn;

    	//The default max bag size is 300 GB. 
    	Double maxBagSizeInGB = maxBagSizeInGBIn == null ? 300 : maxBagSizeInGBIn;
		if(maxBagSizeInGB < 0.0001 || sourceBagSize <= maxBagSizeInGB * SizeHelper.GB){
			String msg = "Max bag size should be no less than 0.0001 GB/104.9 KB, and no greater than the source bag size.";
			splitBagResult.setSuccess(false);
			splitBagResult.addMessage(msg);
			log.info(msg);
			return splitBagResult;
		}
    	Double maxBagSize = maxBagSizeInGB == null? 300 * SizeHelper.GB : maxBagSizeInGB * SizeHelper.GB;
		

		//Verify if the source bag is a complete and valid Bagit bag
		SimpleResult verifyValidResult = sourceBag.verifyValid();
		if(!verifyValidResult.isSuccess()){
			String msg = "The bag is not valid: " + verifyValidResult.toString();
			splitBagResult.setSuccess(false);
			splitBagResult.addMessage(msg);
			log.info(msg);
			return splitBagResult;
		}
		
		//Get all the file paths in manifests
		List<Manifest> manifests = sourceBag.getPayloadManifests();
		Set<String> filePaths = new HashSet<String>();
	    for(Manifest manifest : manifests){
	    	filePaths.addAll(manifest.keySet());	    	
	    }
	    
	    //Get bag files in the source bag
	    List<BagFile> bagFiles = new ArrayList<BagFile>();
	    for(String filePath : filePaths){
	    	BagFile bagFile = sourceBag.getBagFile(filePath);
	    	if(bagFile.getSize() >= maxBagSize) {
	    		String msg = MessageFormat.format("The size of the bag file {0} exceeds the maximum split bag size {1}.", bagFile.getFilepath(), maxBagSize);
				splitBagResult.setSuccess(false);
				splitBagResult.addMessage(msg);
				log.info(msg);
				return splitBagResult;
	    	}
	    	
	    	bagFiles.add(bagFile);
	    }
	    
	    //Group the payload files of the source bag
	    List<BagFileGroup> bagFileGroups = group(bagFiles, maxBagSize);
        
	    //Write each group of payload files to a separate split bag
	    int i = 0;
	    for(BagFileGroup bagFileGroup : bagFileGroups) {
	    	List<BagFile> groupBagFiles = bagFileGroup.getBagFiles();
	    	Bag subBag = bagFactory.createBag(sourceBag.getVersion());	    	
	    	//Add bag info from the source bag to the split bag
	    	BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
	    	subBag.putBagFile(bagInfoTxt);
	    	subBag.getBagInfoTxt().putAll(sourceBag.getBagInfoTxt());
	    	subBag.putBagFile(bagInfoTxt);
	    	subBag.putBagFiles(groupBagFiles);
	    	
	    	//Complete the split bag. The completer will generated/complete tag files for the split bag.
	    	Bag newBag = new DefaultCompleter(bagFactory).complete(subBag);
	    	
	    	//Write the split bag to disk. 
	    	newBag.write(new FileSystemWriter(bagFactory), new File(destBagFile, sourceBagFile.getName()+"_"+i));
	    	i++;
	    }
	    
		return splitBagResult;
	}
    
    protected List<BagFileGroup> group(List<BagFile> bagFiles, Double maxBagSize){
    	
    	Collections.sort(bagFiles, new BagFileSizeReverseComparator());
    	
    	List<BagFileGroup> bagFileGroups = new ArrayList<BagFileGroup>();
    	for(BagFile bagFile : bagFiles) {
    		if(bagFileGroups.isEmpty()){
    			BagFileGroup group = new BagFileGroup(maxBagSize);
    			group.addBagFile(bagFile);
    			bagFileGroups.add(group);
    		} else {
    			boolean foundSpace = false;
    			
    			for(BagFileGroup bagFileGroup : bagFileGroups){
    				if(bagFileGroup.hasSpace(bagFile)) {
    					bagFileGroup.addBagFile(bagFile);
    					foundSpace = true;
    					break;
    				}
    			}
    			
    			if(!foundSpace){
    				BagFileGroup group = new BagFileGroup(maxBagSize);
        			group.addBagFile(bagFile);
        			bagFileGroups.add(group);
    			}
    		}
    	}
    
    	return bagFileGroups;
    }
    
    public class BagFileGroup{
         List<BagFile> bagFiles = new ArrayList<BagFile>();
         Double groupSize = 0.0;
		 Double maxGroupSize = 300 * SizeHelper.GB; 
		 
		 public BagFileGroup(){
			 
		 }
         
		 public BagFileGroup(Double maxGroupSize){
			 this.maxGroupSize = maxGroupSize;
		 }
			 
        public List<BagFile> getBagFiles() {
			return bagFiles;
		}
		public void setBagFiles(List<BagFile> bagFiles) {
			this.bagFiles = bagFiles;
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
		
		public void removeBagFile(BagFile bagFile) {
			this.bagFiles.remove(bagFile);
			this.groupSize -= bagFile.getSize();
		}
		public Double getGroupSize() {
			return groupSize;
		}
		public void setGroupSize(Double groupSize) {
			this.groupSize = groupSize;
		}         
    }
    
    public class BagFileSizeReverseComparator implements Comparator<BagFile>{

		@Override
		public int compare(BagFile bagFile1, BagFile bagFile2) {
			
			return new Long(bagFile2.getSize()).compareTo(new Long(bagFile1.getSize()));
		}
    	
    }

}
