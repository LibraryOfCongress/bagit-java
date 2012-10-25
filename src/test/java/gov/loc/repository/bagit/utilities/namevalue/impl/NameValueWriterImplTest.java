package gov.loc.repository.bagit.utilities.namevalue.impl;

import java.io.ByteArrayOutputStream;

import gov.loc.repository.bagit.utilities.namevalue.NameValueWriter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NameValueWriterImplTest {

	NameValueWriter writer;
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	@Before
	public void setup() {
		writer = new NameValueWriterImpl(out, "utf-8", 79, 2, "test");
	}
	
	@Test
	public void testWrite() throws Exception {
		writer.write("Source-Organization","Spengler University");
		writer.write("External-Description","This collection consists of six large-scale web crawls run against U.S. city web sites from May 2005 to October 2007 as part of the Stanford WebBase project.\nFormat:  ARC files generated from WebBase content.");
		writer.write("Organization-Address","1400 Really Tall Elm Street, Suite 1112, Floor 17, Los Angeles, California, 95014");
		writer.write("Internal-Sender-Description", "Uncompressed greyscale TIFFs.\r\nThey were created from microfilm.");
		writer.close();
		
		String outString = new String(out.toByteArray(), "utf-8");
		assertEquals("Source-Organization: Spengler University\n" +
				"External-Description: This collection consists of six large-scale web crawls\n" +
				"  run against U.S. city web sites from May 2005 to October 2007 as part of the\n" +
				"  Stanford WebBase project.\n" +
				"  \n" +
				"  Format:  ARC files generated from WebBase content.\n" +
				"Organization-Address: 1400 Really Tall Elm Street, Suite 1112, Floor 17, Los\n" +
				"  Angeles, California, 95014\n" +
				"Internal-Sender-Description: Uncompressed greyscale TIFFs.\n" +
				"  \n" + 
				"  They were created from microfilm.\n", 
				outString);
		
	}
}
