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
import java.io.InputStream;
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
	
	private BagFactory bagFactory = new BagFactory();
	
    private static final Log log = LogFactory.getLog(BagSplitter.class);

    public SimpleResult splitBagBySize(File sourceBagFile, File destBagFileIn, Double maxBagSizeInGBIn, boolean keepLowestLevelDir)
	{				
    	SimpleResult splitBagResult = new SimpleResult(true);
		Bag srcBag = this.bagFactory.createBag(sourceBagFile, BagFactory.LoadOption.BY_PAYLOAD_FILES);
		//Verify if the source bag is a complete and valid Bagit bag
		SimpleResult verifyValidResult = srcBag.verifyValid();
		if(!verifyValidResult.isSuccess()){
			this.addMessage(splitBagResult, "The bag is not valid: " + verifyValidResult.toString());
			return splitBagResult;
		}
		this.splitBagBySize1(srcBag, destBagFileIn, maxBagSizeInGBIn, keepLowestLevelDir, splitBagResult);	
		return splitBagResult;
	}
    
    /**
     * Put the bag files of the type specified by fileExtensions to a separate bag. Write the new bag to disk. 
     * 
     * @param srcBag
     * @param fileExtensions
     * @return
     */
    public SimpleResult splitBagByFileType(File sourceBagFile, File destBagFileIn, String[] fileExtensions){
    	SimpleResult splitBagResult = new SimpleResult(true);
    	Bag newBag = this.getSplitBagByFileType(sourceBagFile, destBagFileIn, fileExtensions, splitBagResult);
    	if(! splitBagResult.isSuccess()){
    		return splitBagResult;
    	}    	
	    newBag.write(new FileSystemWriter(this.bagFactory), newBag.getFile());
    	return splitBagResult;
    }
    
    public SimpleResult splitBagBySizeAndFileType(File sourceBagFile, File destBagFileIn, String[] fileExtensions, Double maxBagSizeInGBIn, boolean keepLowestLevelDir){
    	SimpleResult splitBagResult = new SimpleResult(true);
    	Bag newBag = this.getSplitBagByFileType(sourceBagFile, destBagFileIn, fileExtensions, splitBagResult);
    	if(! splitBagResult.isSuccess()) {
    		return splitBagResult;
    	}
    	
	    newBag.write(new FileSystemWriter(this.bagFactory), newBag.getFile());
	    File newDestBagFile = new File(newBag.getFile().getPath().substring(0, newBag.getFile().getPath().lastIndexOf('/')));
    	this.splitBagBySize1(newBag, newDestBagFile, maxBagSizeInGBIn, keepLowestLevelDir, splitBagResult);
    	this.deleteDir(newBag.getFile());
    	return splitBagResult;
    }
    
    private void splitBagBySize1(Bag srcBag, File destBagFileIn, Double maxBagSizeInGBIn, boolean keepLowestLevelDir, SimpleResult splitBagResult)
	{				
    	List<BagFile> bagFiles = new ArrayList<BagFile>();
    	
		Double sourceBagSize = new Double(srcBag.getBagInfoTxt().getPayloadOxum());

		//The default dest of split bags is parentDirOfSourceBag/SourceBagName_split
    	File destBagFile = destBagFileIn == null ? new File(srcBag.getFile() + "_split") : destBagFileIn;

    	//The default max bag size is 300 GB. 
    	Double maxBagSizeInGB = maxBagSizeInGBIn == null ? 300 : maxBagSizeInGBIn;
		if(maxBagSizeInGB < 0.0001 || sourceBagSize <= maxBagSizeInGB * SizeHelper.GB){
			this.addMessage(splitBagResult, "Max bag size should be no less than 0.0001 GB/104.9 KB, and no greater than the source bag size.");
			return;
		}
	    Double maxBagSize =  maxBagSizeInGB != null ? maxBagSizeInGB * SizeHelper.GB : 300 * SizeHelper.GB;			
		
		//Get all the file paths in manifests
		List<Manifest> manifests = srcBag.getPayloadManifests();
		Set<String> filePaths = new HashSet<String>();
	    for(Manifest manifest : manifests){
	    	filePaths.addAll(manifest.keySet());	    	
	    }
	    
	    //Sort bag files in the source bag
	    this.sortBagFiles(new File(srcBag.getFile(), srcBag.getBagConstants().getDataDirectory()), keepLowestLevelDir, splitBagResult, srcBag, bagFiles, maxBagSize);
	    if(! splitBagResult.isSuccess()){
	    	return;
	    }
	    
	    //Group the payload files of the source bag
	    List<BagFileGroup> bagFileGroups = group(bagFiles, maxBagSize);
        
	    //Write each group of payload files to a separate split bag
	    int i = 0;
	    for(BagFileGroup bagFileGroup : bagFileGroups) {
	    	List<BagFile> groupBagFiles = bagFileGroup.getBagFiles();
	    	Bag subBag = bagFactory.createBag(srcBag.getVersion());	    	
	    	BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
	    	subBag.putBagFile(bagInfoTxt);
	    	//Add bag info from the source bag to the split bag
	    	subBag.getBagInfoTxt().putAll(srcBag.getBagInfoTxt());
	    	for(BagFile bagFile : groupBagFiles){
	    		if(bagFile instanceof LowestLevelBagDir){
	    			subBag.putBagFiles(((LowestLevelBagDir) bagFile).getBagFiles());
	    		} else {
	    			subBag.putBagFile(bagFile);
	    		}
	    	}
	    	
	    	//Complete the split bag. The completer will generated/complete tag files for the split bag.
	    	Bag newBag = new DefaultCompleter(bagFactory).complete(subBag);
	    	
	    	//Write the split bag to disk. 
	    	newBag.write(new FileSystemWriter(bagFactory), new File(destBagFile, srcBag.getFile().getName()+"_"+i));
	    	i++;
	    }
	    
		return;
	}
   
    /**
     * Put the bag files of the type specified by fileExtensions to a new bag. 
     * 
     * @param srcBag
     * @param fileExtensions
     * @return 
     */
    private Bag getSplitBagByFileType(File sourceBagFile, File destBagFileIn, String[] fileExtensionsIn, SimpleResult splitBagResult)
	{				
    	if(fileExtensionsIn == null || fileExtensionsIn.length <= 0){
    		this.addMessage(splitBagResult, "File extensions should not be null or empty.");
    		return null;
    	}
    	Set<String> fileExtensions = new HashSet<String>();
    	for(String fileEx : fileExtensionsIn){
    		fileExtensions.add(fileEx.trim());
    	}
    	
    	//The default dest of split bags is parentDirOfSourceBag/SourceBagName_split
    	File destBagFile = destBagFileIn == null ? new File(sourceBagFile.getAbsoluteFile() + "_split") : destBagFileIn;
    	
    	//Load source bag
		Bag srcBag = this.bagFactory.createBag(sourceBagFile, BagFactory.LoadOption.BY_PAYLOAD_FILES);
		
    	//Verify if the source bag is a complete and valid Bagit bag
		SimpleResult verifyValidResult = srcBag.verifyValid();
		if(!srcBag.verifyValid().isSuccess()){
			this.addMessage(splitBagResult, "The bag is not valid: " + verifyValidResult.toString());
			return null;
		}
		
		//Get all the file paths in manifests
		List<Manifest> manifests = srcBag.getPayloadManifests();
		Set<String> filePaths = new HashSet<String>();
	    for(Manifest manifest : manifests){
	    	filePaths.addAll(manifest.keySet());	    	
	    }
	    
	    //Sort out targeted bag files in the source bag
	    List<BagFile> targetedBagFiles = new ArrayList<BagFile>();
	    for(String filePath : filePaths){
	    	String fileExtension = filePath.substring(filePath.lastIndexOf('.') + 1);
	    	for(String fileEx : fileExtensions){
	    		if(fileEx.equalsIgnoreCase(fileExtension)){
	    			targetedBagFiles.add(srcBag.getBagFile(filePath));
	    			break;
	    		}
	    	}
	    }
	   
	    //Put the targeted bag files to a new bag
	    Bag subBag = this.bagFactory.createBag(srcBag.getVersion());	    	
	    BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
	    subBag.putBagFile(bagInfoTxt);
	    //Add bag info from the source bag to the new bag
	    subBag.getBagInfoTxt().putAll(srcBag.getBagInfoTxt());
	    subBag.putBagFiles(targetedBagFiles);
	    //Set file path for the new bag. The new bag name suffix is the concatenation of targeted file extensions. 
	    StringBuffer bag_suffix = new StringBuffer("");
	    for(String fileEx : fileExtensions) {
	    	bag_suffix.append("_" +fileEx);
	    }
	    subBag.setFile(new File(destBagFile, sourceBagFile.getName() + bag_suffix.toString()));
	    	
	    //Complete the new bag. The completer will generated/complete tag files for the split bag.
	    return new DefaultCompleter(this.bagFactory).complete(subBag);
	}
    
    /**
     * Group the bag files.  The size of each group should be no greater than the max bag size.
     * 
     * @param bagFiles
     * @param maxBagSize
     * @return
     */
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
    
    private class LowestLevelBagDir implements BagFile{
		private String filePath;
    	private File file;
		private List<BagFile> bagFiles = new ArrayList<BagFile>();
		
		public LowestLevelBagDir(String filePath, File file) {
			if(!isLowestLevelDir(file)){
				throw new RuntimeException("The file is not a lowest level directory.");
			}
			this.filePath = filePath;
			this.file = file;			
		}

		public List<BagFile> getBagFiles(){
			return this.bagFiles;
		}
		
		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public String getFilepath() {
			return this.filePath;
		}

		@Override
		public long getSize() {
			long length = 0L;
			File[] files = this.file.listFiles();
			for(File fileItem : files) {
				length += fileItem.length();
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
    
    /**
     * A directory is considered a lowest level directory if it contains only files.
     * @param file
     * @return
     */
    private boolean isLowestLevelDir(File file){
    	if(file == null || file.isFile() || !file.exists()){
    		return false;
    	}
    	File[] files = file.listFiles();
    	for(File fileItem : files) {
    		if(fileItem.isDirectory()){
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * A bag file list contains lowest level directories, and files locating at the same level as a lowest level directory.
     * 
     * All the files in the lowest level directory is considered as a whole if the keepLowestDir flag is indicated 
     * when splitting a bag.
     *    
     * @param file
     */
    private void sortBagFiles(File file, boolean keepLowestLevelDir, SimpleResult result, Bag scrBag, List<BagFile> bagFiles, Double maxBagSize){
    	
    	//If the file is a lowest level directory, add it to the list and return
    	if(keepLowestLevelDir && isLowestLevelDir(file)){
    		LowestLevelBagDir lowestLevelBagDir = new LowestLevelBagDir(this.getBagFilePath(scrBag, file), file);
    		if(lowestLevelBagDir.getSize() >= maxBagSize) {
	    		this.addMessage(result, MessageFormat.format("The size of the lowest level directory {0} exceeds the maximum split bag size {1}.", lowestLevelBagDir.getFilepath(), SizeHelper.getSize((long)maxBagSize.longValue())));
				return;
	    	}
    		File[] files = file.listFiles();
        	for(File fileItem : files) {
        		lowestLevelBagDir.addBagFile(scrBag.getBagFile(this.getBagFilePath(scrBag, fileItem)));
        	}
    		bagFiles.add(lowestLevelBagDir);

    		return;
    	}
    	
    	//Otherwise, add all the files in the directory to the list, and repeat the operation for its sub directories.
    	File[] files = file.listFiles();
    	for(File fileItem : files) {
    		if(fileItem.isFile()){
    			if(fileItem.length() >= maxBagSize) {
    	    		this.addMessage(result, MessageFormat.format("The size of the file {0} exceeds the maximum split bag size {1}.", fileItem.getPath(),SizeHelper.getSize((long)(maxBagSize.longValue()))));    				
    				return;
    	    	}
    			bagFiles.add(scrBag.getBagFile(this.getBagFilePath(scrBag, fileItem)));

    		}else{
    			sortBagFiles(fileItem, keepLowestLevelDir, result, scrBag, bagFiles, maxBagSize);
    		}
    	}
    }
    
    private String getBagFilePath(Bag scrBag, File file){
    	return file.getPath().substring(file.getPath().lastIndexOf(scrBag.getBagConstants().getDataDirectory()));
    }
    
    private void addMessage(SimpleResult result, String msg){
    	result.setSuccess(false);
    	result.addMessage(msg);
		log.info(msg);
    }
    
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    } 
}
