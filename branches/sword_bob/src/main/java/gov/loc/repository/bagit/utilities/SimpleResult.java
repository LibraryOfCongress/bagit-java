package gov.loc.repository.bagit.utilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SimpleResult {
	private boolean isSuccess;
	private List<String> messages = new ArrayList<String>();
	
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
	
	public String messagesToString()
	{
		String messageString = "";
		for(String message : messages) {
			if (messageString.length() > 0) {
				messageString += " ";
			}
			messageString += message;
		}
		return messageString;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("Result is {0}. {1}", this.isSuccess, this.messagesToString());
	}
	
	public void merge(SimpleResult result) {
		if (! (this.isSuccess() && result.isSuccess)) {
			this.isSuccess = false;
		}
		this.messages.addAll(result.messages);
	}
	
	public List<String> getMessages() {
		return this.messages;
	}
}
