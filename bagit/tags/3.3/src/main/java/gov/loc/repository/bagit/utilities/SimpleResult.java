package gov.loc.repository.bagit.utilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SimpleResult {
	
	private boolean isSuccess;
	private List<String> messages = new ArrayList<String>();
	
	private static Integer MAX_MESSAGES = 100;
	
	public SimpleResult(boolean isSuccess) {
		this.isSuccess = isSuccess;			
	}

	public SimpleResult(boolean isSuccess, String message) {
		this.isSuccess = isSuccess;
		this.messages.add(message);
	}

	public void addMessage(String message) {
		this.messages.add(message);
	}
	
	public boolean isSuccess()
	{
		return this.isSuccess;
	}
	
	public String messagesToString(int maxMessages)
	{
		StringBuffer buf = new StringBuffer();
		int count = 0;
		for(String message : messages) {
			count++;
			if (count > maxMessages) {
				buf.append("And others.");
				break;
			}
			if (buf.length() > 0) buf.append(" ");
			buf.append(message);
		}
		String messageString = buf.toString();
		return messageString;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	@Override
	public String toString() {
		return this.toString(MAX_MESSAGES);
	}

	public String toString(int maxMessages) {
		String msg = MessageFormat.format("Result is {0}. {1}", this.isSuccess, this.messagesToString(maxMessages));
		return msg;
	}

	
	public void merge(SimpleResult result) {
		if (result == null) {
			return;
		}
		if (! (this.isSuccess() && result.isSuccess)) {
			this.isSuccess = false;
		}
		this.messages.addAll(result.messages);
	}
	
	public List<String> getMessages() {
		return this.messages;
	}
}
