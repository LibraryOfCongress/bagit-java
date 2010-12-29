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
	
	private Bag sourceBag;
	private Double maxBagSize = 300 * SizeHelper.GB; 
	private List<BagFile> bagFiles = new ArrayList<BagFile>();
	private SimpleResult splitBagResult = new SimpleResult(true);
	
    private static final Log log = LogFactory.getLog(BagSplitter.class);

    public SimpleResult splitBagBySize(File sourceBagFile, File destBagFileIn, Double maxBagSizeInGBIn, boolean keepLowestLevelDir)
	{				
    	//Load source bag
    	BagFactory bagFactory = new BagFactory();
		this.sourceBag = bagFactory.createBag(sourceBagFile, BagFactory.LoadOption.BY_PAYLOAD_FILES);
		Double sourceBagSize = new Double(sourceBag.getBagInfoTxt().getPayloadOxum());

		//The default dest of split bags is parentDirOfSourceBag/SourceBagName_split
    	File destBagFile = destBagFileIn == null ? new File(sourceBagFile.getAbsoluteFile() + "_split") : destBagFileIn;

    	//The default max bag size is 300 GB. 
    	Double maxBagSizeInGB = maxBagSizeInGBIn == null ? 300 : maxBagSizeInGBIn;
		if(maxBagSizeInGB < 0.0001 || sourceBagSize <= maxBagSizeInGB * SizeHelper.GB){
			String msg = "Max bag size should be no less than 0.0001 GB/104.9 KB, and no greater than the source bag size.";
			splitBagResult.setSuccess(false);
			splitBagResult.addMessage(msg);
			System.out.println(msg);
			log.info(msg);
			return splitBagResult;
		}
		if(maxBagSizeInGB != null){
	    	maxBagSize =  maxBagSizeInGB * SizeHelper.GB;			
		}
		
		//Verify if the source bag is a complete and valid Bagit bag
		SimpleResult verifyValidResult = sourceBag.verifyValid();
		if(!verifyValidResult.isSuccess()){
			String msg = "The bag is not valid: " + verifyValidResult.toString();
			splitBagResult.setSuccess(false);
			splitBagResult.addMessage(msg);
			log.info(msg);
			System.out.println(msg);
			return splitBagResult;
		}
		
		//Get all the file paths in manifests
		List<Manifest> manifests = sourceBag.getPayloadManifests();
		Set<String> filePaths = new HashSet<String>();
	    for(Manifest manifest : manifests){
	    	filePaths.addAll(manifest.keySet());	    	
	    }
	    
	    //Sort bag files in the source bag
	    this.sortBagFiles(new File(sourceBagFile, sourceBag.getBagConstants().getDataDirectory()), keepLowestLevelDir);
	    if(! splitBagResult.isSuccess()){
	    	return splitBagResult;
	    }
	    
	    //Group the payload files of the source bag
	    List<BagFileGroup> bagFileGroups = group(bagFiles, maxBagSize);
        
	    //Write each group of payload files to a separate split bag
	    int i = 0;
	    for(BagFileGroup bagFileGroup : bagFileGroups) {
	    	List<BagFile> groupBagFiles = bagFileGroup.getBagFiles();
	    	Bag subBag = bagFactory.createBag(sourceBag.getVersion());	    	
	    	BagInfoTxt bagInfoTxt = subBag.getBagPartFactory().createBagInfoTxt();
	    	subBag.putBagFile(bagInfoTxt);
	    	//Add bag info from the source bag to the split bag
	    	subBag.getBagInfoTxt().putAll(sourceBag.getBagInfoTxt());
	    	subBag.putBagFile(bagInfoTxt);
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
	    	newBag.write(new FileSystemWriter(bagFactory), new File(destBagFile, sourceBagFile.getName()+"_"+i));
	    	i++;
	    }
	    
		return splitBagResult;
	}
    
    /**
     * Group the bag files.  The size of each group should be no greater than the max bag size.
     * 
     * @param bagFiles
     * @param maxBagSize
     * @return
     */
    protected List<BagFileGroup> group(List<BagFile> bagFiles, Double maxBagSize){
    	
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
    protected boolean isLowestLevelDir(File file){
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
    private void sortBagFiles(File file, boolean keepLowestLevelDir){
    	
    	//If the file is a lowest level directory, add it to the list and return
    	if(keepLowestLevelDir && isLowestLevelDir(file)){
    		LowestLevelBagDir lowestLevelBagDir = new LowestLevelBagDir(this.getBagFilePath(file), file);
    		if(lowestLevelBagDir.getSize() >= maxBagSize) {
	    		String msg = MessageFormat.format("The size of the lowest level directory {0} exceeds the maximum split bag size {1}.", lowestLevelBagDir.getFilepath(), SizeHelper.getSize((long)maxBagSize.longValue()));
				splitBagResult.setSuccess(false);
				splitBagResult.addMessage(msg);
				log.info(msg);
				return;
	    	}
    		File[] files = file.listFiles();
        	for(File fileItem : files) {
        		lowestLevelBagDir.addBagFile(sourceBag.getBagFile(this.getBagFilePath(fileItem)));
        	}
    		bagFiles.add(lowestLevelBagDir);

    		return;
    	}
    	
    	//Otherwise, add all the files in the directory to the list, and repeat the operation for its sub directories.
    	File[] files = file.listFiles();
    	for(File fileItem : files) {
    		if(fileItem.isFile()){
    			if(fileItem.length() >= maxBagSize) {
    	    		String msg = MessageFormat.format("The size of the file {0} exceeds the maximum split bag size {1}.", fileItem.getPath(),SizeHelper.getSize((long)(maxBagSize.longValue())));
    				splitBagResult.setSuccess(false);
    				splitBagResult.addMessage(msg);
    				log.info(msg);
    				return;
    	    	}
    			bagFiles.add(sourceBag.getBagFile(this.getBagFilePath(fileItem)));

    		}else{
    			sortBagFiles(fileItem, keepLowestLevelDir);
    		}
    	}
    }
    
    private String getBagFilePath(File file){
    	return file.getPath().substring(file.getPath().lastIndexOf(sourceBag.getBagConstants().getDataDirectory()));
    }
}
