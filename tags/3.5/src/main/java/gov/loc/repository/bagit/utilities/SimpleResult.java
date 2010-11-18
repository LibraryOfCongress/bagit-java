package gov.loc.repository.bagit.utilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SimpleResult {
	
	private boolean isSuccess;
	private List<String> messages = new ArrayList<String>();
	
	public static Integer DEFAULT_MAX_MESSAGES = 100;
	public static String DEFAULT_DELIM = " ";
	
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
		return this.messagesToString(maxMessages, DEFAULT_DELIM);
	}
	
	public String messagesToString(int maxMessages, String delim)
	{
		StringBuffer buf = new StringBuffer();
		int count = 0;
		for(String message : messages) {
			count++;
			if (count > maxMessages) {
				buf.append(delim + "And others.");
				break;
			}
			if (buf.length() > 0) buf.append(delim);
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
		return this.toString(DEFAULT_MAX_MESSAGES, DEFAULT_DELIM);
	}

	public String toString(int maxMessages) {
		return this.toString(maxMessages, DEFAULT_DELIM);
	}

	public String toString(int maxMessages, String delim) {
		if (this.messages.isEmpty()) delim = "";
		String msg = MessageFormat.format("Result is {0}.{1}{2}", this.isSuccess, delim, this.messagesToString(maxMessages, delim));
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
