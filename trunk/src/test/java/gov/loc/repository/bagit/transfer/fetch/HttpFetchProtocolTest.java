package gov.loc.repository.bagit.transfer.fetch;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.BagFetcher;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.transfer.NullFetchContext;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import static org.junit.Assert.*;

@RunWith(JMock.class)
public class HttpFetchProtocolTest
{
    private static final URI TEST1_URI = newV96Uri("data/test%201.txt");
    
    private static Server webServer;
    private Mockery context = new JUnit4Mockery();
    private File testDataRoot = new File("target/unittestdata/HttpFetchProtocolTest");
    private BagFactory bagFactory = new BagFactory();
    
    @BeforeClass
    public static void startJetty() throws Exception
    {
        webServer = new Server(8989);
        WebAppContext context = new WebAppContext(ResourceHelper.TEST_DATA_DIR.getAbsolutePath(), "/");
        webServer.addHandler(context);
        webServer.start();
    }
    
    @AfterClass
    public static void stopJetty() throws Exception
    {
        if (webServer != null)
            webServer.stop();
    }
    
    @After
    public void cleanUpTestData() throws Exception
    {
        FileUtils.deleteDirectory(this.testDataRoot);
    }
    
    private static URI newV96Uri(String path)
    {
        return URI.create("http://localhost:8989/bags/v0_96/bag-with-space/" + path);
    }
    
    @Test
    public void testJettyIsUp() throws Exception
    {
        // Just do a gross test to make sure Jetty is working and responding
        // to requests.
        URL url = new URL("http://localhost:8989/bags/v0_96/bag/bagit.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        
        try
        {
            String line = reader.readLine();
            assertEquals("BagIt-Version: 0.96", line);
            
            line = reader.readLine();
            assertEquals("Tag-File-Character-Encoding: UTF-8", line);
            
            line = reader.readLine();
            assertNull(line);
        }
        finally
        {
            reader.close();
        }
    }
    
    @Test
    public void testFetchesFiles() throws Exception
    {
    	final FetchedFileDestination destination = context.mock(FetchedFileDestination.class);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        context.checking(new Expectations() {{
            oneOf(destination).openOutputStream(false); will(returnValue(stream));
            allowing(destination).getFilepath(); will(returnValue("/tmp/unittest"));
        }});
        
        HttpFetchProtocol protocol = new HttpFetchProtocol();
        FileFetcher fetcher = protocol.createFetcher(TEST1_URI, null);
        fetcher.fetchFile(TEST1_URI, null, destination, new NullFetchContext());
        
        assertEquals(IOUtils.toString(TEST1_URI.toURL().openStream()), new String(stream.toByteArray()));
    }
    
    @Test
    public void testWorksWithBagFetcher() throws Exception
    {
        File testDestination = new File(this.testDataRoot, "testWorksWithBagFetcher");
        System.out.println("Writing to: " + testDestination);
        
        Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_96/holey-bag"));
        Writer writer = new HoleyWriter(this.bagFactory);
        writer.write(bag, testDestination);
        
        BagFetcher fetcher = new BagFetcher(this.bagFactory);
        fetcher.setNumberOfThreads(1);
        fetcher.registerProtocol("http", new HttpFetchProtocol());
        
        fetcher.fetch(bag, new FileSystemFileDestination(testDestination), false);

        Bag newBag = this.bagFactory.createBag(testDestination);
        assertTrue(newBag.verifyValid().isSuccess());
    }
    
    private static class HoleyWriter extends FileSystemWriter
    {    	
        public HoleyWriter(BagFactory bagFactory) {
			super(bagFactory);
		}

		@Override
        public void visitPayload(BagFile bagFile)
        {
            // Do nothing.
        }
    }
}
