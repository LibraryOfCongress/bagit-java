package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.writer.Writer;

public abstract class AbstractWriter extends AbstractBagVisitor implements Writer {

	private CancelIndicator cancelIndicator = null;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
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
	
	protected boolean isCancelled()
	{
		return this.cancelIndicator != null && this.cancelIndicator.performCancel();
	}
	
	@Override
	public void addProgressListener(ProgressListener progressListener) {
		this.progressListeners.add(progressListener);
	}

	@Override
	public void removeProgressListener(ProgressListener progressListener) {
		this.progressListeners.remove(progressListener);
	}
	
	protected void progress(String activity, String item, int count, int total)
	{
		for (ProgressListener listener : this.progressListeners)
		{
			listener.reportProgress(activity, item, count, total);
		}
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
