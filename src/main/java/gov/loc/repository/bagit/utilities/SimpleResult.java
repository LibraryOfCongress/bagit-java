package gov.loc.repository.bagit.utilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleResult {
	
	protected boolean isSuccess = false;
	//Map<code, Map<subject, List<SimpleMessage>>
	protected Map<String, Map<String, SimpleMessage>> messages = new HashMap<String, Map<String,SimpleMessage>>();
	
	public static Integer DEFAULT_MAX_MESSAGES = 100;
	public static String DEFAULT_DELIM = " ";
	
	public SimpleResult() {
	}	
	
	public SimpleResult(boolean isSuccess) {
		this.isSuccess = isSuccess;			
	}

	public SimpleResult(boolean isSuccess, String message) {
		this.isSuccess = isSuccess;
		this.addSimpleMessage(new SimpleMessage(message));
	}

	public SimpleResult(boolean isSuccess, SimpleMessage message) {
		this.isSuccess = isSuccess;
		this.addSimpleMessage(message);
	}
	
	public SimpleResult(boolean isSuccess, Collection<String> messages) {
		this.isSuccess = isSuccess;
		this.addMessages(messages);
	}
		
	public void addMessage(String message) {
		this.addSimpleMessage(new SimpleMessage(message));
	}
	
	public void addMessage(String code, String message) {
		this.addSimpleMessage(new SimpleMessage(code, message));
	}
	
	public void addMessage(String code, String message, String subject) {
		this.addSimpleMessage(new SimpleMessage(code, message, subject));
	}

	public void addMessage(String code, String message, String subject, String object) {
		this.addSimpleMessage(new SimpleMessage(code, message, subject, object));
	}

	public void addMessage(String code, String message, String subject, Collection<String> objects) {
		this.addSimpleMessage(new SimpleMessage(code, message, subject, objects));
	}
	
	public void addSimpleMessage(SimpleMessage message) {
		Map<String,SimpleMessage> subjectMap = this.messages.get(message.getCode());
		if (subjectMap == null) {
			subjectMap = new HashMap<String,SimpleMessage>();
			this.messages.put(message.getCode(), subjectMap);
		}
		SimpleMessage existingMessage = subjectMap.get(message.getSubject());
		if (existingMessage != null) {
			existingMessage.addObjects(message.getObjects());
		} else {
			subjectMap.put(message.getSubject(), message);
		}
		
	}
	
	public void addMessages(Collection<String> messages) {
		for(String message : messages) {
			this.addSimpleMessage(new SimpleMessage(message));
		}
	}
	
	public void addSimpleMessages(Collection<SimpleMessage> messages) {
		for(SimpleMessage message : messages) {
			this.addSimpleMessage(message);
		}
	}
	
	public boolean isSuccess() {
		return this.isSuccess;
	}

	public String messagesToString(int maxMessages) {
		return this.messagesToString(maxMessages, DEFAULT_DELIM);
	}
	
	public String messagesToString(int maxMessages, String delim) {
		StringBuffer buf = new StringBuffer();
		int count = 0;
		for(SimpleMessage message : this.getSimpleMessages()) {
			count++;
			if (count > maxMessages) {
				buf.append(delim + "And others.");
				break;
			}
			if (buf.length() > 0) buf.append(delim);
			buf.append(message.toString());
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
		this.addSimpleMessages(result.getSimpleMessages());
	}
	
	public List<String> getMessages() {
		List<String> messageStrings = new ArrayList<String>();
		for(SimpleMessage message : getSimpleMessages()) {
			messageStrings.add(message.toString());
		}
		return messageStrings;
	}
	
	public List<SimpleMessage> getSimpleMessages() {
		List<SimpleMessage> messages = new ArrayList<SimpleMessage>();
		for(Map<String,SimpleMessage> subjectMap : this.messages.values()) {
			messages.addAll(subjectMap.values());
		}
		return messages;
	}
	
	public List<SimpleMessage> getSimpleMessage(String code) {
		List<SimpleMessage> messages = new ArrayList<SimpleMessage>();
		Map<String, SimpleMessage> subjectMap = this.messages.get(code);
		if (subjectMap != null) {
			messages.addAll(subjectMap.values());
		}
		return messages;
	}
	
	public SimpleMessage getSimpleMessage(String code, String subject) {
		Map<String, SimpleMessage> subjectMap = this.messages.get(code);
		if (subjectMap != null) {
			return subjectMap.get(subject);
		}
		return null;
	}
	
	public boolean hasSimpleMessage(String code) {
		return ! this.getSimpleMessage(code).isEmpty();		
	}

	public boolean hasSimpleMessage(String code, String object) {
		return this.getSimpleMessage(code, object) != null;
	}
	
	public class SimpleMessage {
		private String code = null;
		private String message = null;
		private String subject = null;
		private Set<String> objects = null;
		
		public SimpleMessage() {
		}

		public SimpleMessage(String message) {
			this.message = message;
		}
		
		public SimpleMessage(String code, String message) {
			this.code = code;
			this.message = message;
		}

		public SimpleMessage(String code, String message, String subject) {
			this.code = code;
			this.message = message;
			this.subject = subject;
		}

		public SimpleMessage(String code, String message, String subject, String object) {
			this.code = code;
			this.message = message;
			this.subject = subject;
			this.objects = new HashSet<String>();
			this.objects.add(object);
		}

		public SimpleMessage(String code, String message, String subject, Collection<String> objects) {
			this.code = code;
			this.message = message;
			this.subject = subject;
			this.objects = new HashSet<String>();
			this.objects.addAll(objects);
		}

		
		public String getCode() {
			return code;
		}
		
		public void setCode(String code) {
			this.code = code;
		}
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
		public Set<String> getObjects() {
			return objects;
		}
		
		public void addObject(String object) {
			if (this.objects == null) {
				this.objects = new HashSet<String>();				
			}
			this.objects.add(object);
		}

		public void addObjects(Collection<String> objects) {
			if (this.objects == null) {
				this.objects = new HashSet<String>();				
			}
			this.objects.addAll(objects);
		}

		
		public void setObjects(Set<String> objects) {
			this.objects = objects;
		}

		public String getSubject() {
			return subject;
		}
		
		public void setSubject(String subject) {
			this.subject = subject;
		}
		
		@Override
		public String toString() {
			if (this.message != null) {
				if (this.subject == null) {
					return this.message;
				}
				if (this.objects == null) {
					return MessageFormat.format(this.message, this.subject);
				}
				return MessageFormat.format(this.message, this.subject, this.objects);
			}				
			return super.toString();
		}
	}
}
