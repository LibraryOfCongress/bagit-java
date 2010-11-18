package gov.loc.repository.bagit.utilities.namevalue;

import java.text.MessageFormat;
import java.util.Iterator;

public interface NameValueReader extends Iterator<NameValueReader.NameValue> {

	public class NameValue {
		private String name;
		private String value;
		
		public NameValue(String name, String value) {
			this.name = name;
			this.value = value;
		}
			
		public NameValue()	{			
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("Name is {0}. Value is {1}.", this.name, this.value);
		}
	}	
}
