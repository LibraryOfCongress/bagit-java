package gov.loc.repository.bagit.impl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

public class PreBagImpl implements PreBag {

	private static final Log log = LogFactory.getLog(PreBagImpl.class);
	
	BagFactory bagFactory;
	File dir;
	List<File> tagFiles = new ArrayList<File>();
	List<String> ignoreDirs = new ArrayList<String>();
	
	public PreBagImpl(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}
	
	@Override
	public File getFile() {
		return this.dir;
	}

	@Override
	public void setIgnoreAdditionalDirectories(List<String> dirs) {
		this.ignoreDirs = dirs;
	}
	
	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory) {
		return this.makeBagInPlace(version, retainBaseDirectory, false, new DefaultCompleter(this.bagFactory));

	}

	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory, Completer completer) {
		return this.makeBagInPlace(version, retainBaseDirectory, false, completer);
	}
	
	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory,
			boolean keepEmptyDirectories) {
		return this.makeBagInPlace(version, retainBaseDirectory, keepEmptyDirectories, new DefaultCompleter(this.bagFactory));
	}
	
	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory, boolean keepEmptyDirectories, Completer completer) {
		log.info(MessageFormat.format("Making a bag in place at {0}", this.dir));
		File dataDir = new File(this.dir, this.bagFactory.getBagConstants(version).getDataDirectory());
		log.trace("Data directory is " + dataDir);
		try {
			//If there is no data direct
			if (! dataDir.exists()) {
				log.trace("Data directory does not exist");
				//If retainBaseDirectory
				File moveToDir = dataDir;
				if (retainBaseDirectory) {
					//Create new base directory in data directory
					moveToDir = new File(dataDir, this.dir.getName());
					//Move contents of base directory to new base directory
				}
				log.trace("Move to dir is " + moveToDir);
				for(File file : this.dir.listFiles()) {
					if (! (file.equals(dataDir) || (file.isDirectory() && this.ignoreDirs.contains(file.getName())))) {
						FileUtils.moveToDirectory(file, moveToDir, true);
					}
				}
				
			} else {
				if (! dataDir.isDirectory()) throw new RuntimeException(MessageFormat.format("{0} is not a directory", dataDir));
				//Look for additional, non-ignored files
				for(File file : this.dir.listFiles()) {
					//If there is a directory that isn't the data dir and isn't ignored and pre v0.97 then exception
					if (file.isDirectory() 
							&& (! file.equals(dataDir)) 
							&& ! this.ignoreDirs.contains(file.getName())
							&& (Version.V0_93 == version || Version.V0_94 == version || Version.V0_95 == version || Version.V0_96 == version)) {
						throw new RuntimeException("Found additional directories in addition to existing data directory.");
					}
				}
				
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		//Handle empty directories
		if (keepEmptyDirectories) {
			this.addKeep(dataDir);
		}
		
		//Copy the tags
		for(File tagFile : this.tagFiles) {
			log.trace(MessageFormat.format("Copying tag file {0} to {1}", tagFile, this.dir));
			try {
				FileUtils.copyFileToDirectory(tagFile, this.dir);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
						
		//Create a bag
		Bag bag = this.bagFactory.createBagByPayloadFiles(this.dir, version, this.ignoreDirs);
		//Complete the bag
		bag = bag.makeComplete(completer);
		//Write the bag
		return bag.write(new FileSystemWriter(this.bagFactory), this.dir);
	}

	@Override
	public void setFile(File dir) {
		if (! dir.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist", dir));
		}
		if (! dir.isDirectory()) {
			throw new RuntimeException(MessageFormat.format("{0} is not a directory", dir));
		}
		this.dir = dir;
	}

	@Override
	public List<File> getTagFiles() {
		return this.tagFiles;
	}

	@Override
	public void setTagFiles(List<File> tagFiles) {
		this.tagFiles = tagFiles;		
	}
	
	private void addKeep(File file) {
		if (file.isDirectory() && ! this.ignoreDirs.contains(file.getName())) {
			//If file is empty, add .keep
			File[] children = file.listFiles();
			if (children.length == 0) {
				log.info("Adding .keep file to " + file.toString());
				try {
					FileUtils.touch(new File(file, ".keep"));
				} catch (IOException e) {
					throw new RuntimeException("Error adding .keep file to " + file.toString(), e);
				}
			} else {
				//Otherwise, recurse over children
				for(File childFile : children) {
					addKeep(childFile);
				}
			}
		}
		//Otherwise do nothing
	}

}
