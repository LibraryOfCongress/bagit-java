package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.writer.Writer;

public abstract class AbstractWriter extends AbstractBagVisitor implements Writer {

	protected CancelIndicator cancelIndicator = null;
	protected ProgressListener progressListener = null;
	protected BagFactory bagFactory;
	
	public AbstractWriter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;		
	}
	
	@Override
	public CancelIndicator getCancelIndicator() {
		return this.cancelIndicator;
	}
	
	@Override
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	protected File getTempFile(File file) {
		return new File(file.getPath() + ".biltemp");
	}
	
	protected void switchTemp(File file) {
		File tempFile = this.getTempFile(file);
		if (! tempFile.exists()) {
			throw new RuntimeException(MessageFormat.format("Temp file {0} for {1} doesn't exist.", tempFile, file));
		}
		try {
			if (file.exists()) {
					FileUtils.forceDelete(file);
			}
			FileUtils.moveFile(tempFile, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
