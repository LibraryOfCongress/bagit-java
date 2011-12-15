package gov.loc.repository.bagit.writer.impl;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.ZipWriter;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class ZipBagWriterTest extends AbstractWriterTest {

	File bagFile;
	
	@Before
    @Override
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.zip");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		return new ZipWriter(bagFactory);

	}
	
	@Test
	public void testCompress() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		try {
			ZipWriter writer = new ZipWriter(bagFactory);
			File uncompressedBagFile = new File(ResourceHelper.getFile("bags"), "uncompressed.zip");		
			writer.write(bag, uncompressedBagFile);
			
			File compressedBagFile = new File(ResourceHelper.getFile("bags"), "compressed.zip");
			writer.setCompressionLevel(ZipWriter.DEFAULT_COMPRESSION_LEVEL);
			writer.write(bag, compressedBagFile);
			
			assertTrue(compressedBagFile.length() < uncompressedBagFile.length());
		} finally {
			bag.close();
		}
		
	}
	

}
