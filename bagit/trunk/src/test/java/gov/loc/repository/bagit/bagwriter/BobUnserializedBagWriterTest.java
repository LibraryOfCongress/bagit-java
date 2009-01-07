package gov.loc.repository.bagit.bagwriter;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.IOException;

import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BobUnserializedBagWriterTest {

	SimpleHttpServer server;
	TestRequestHandler handler;
	String baseURL;
	
	@Before
	public void setUp() throws Exception {
		this.server = new SimpleHttpServer();
		this.baseURL = "http://localhost:" + this.server.getLocalPort() + "/";
		this.handler = new TestRequestHandler(baseURL);
		this.server.setRequestHandler(this.handler);
	}
	
	@After
	public void teardown() throws Exception {
		this.server.destroy();
	}
	
	@Test(timeout=30000)
	public void testWriter() throws Exception {
		Bag bag = BagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest"));
		assertTrue(bag.isValid().isSuccess());

		BobUnserializedBagWriter writer = new BobUnserializedBagWriter(this.baseURL, false, null, null);
		bag.write(writer);
		
		while(! this.handler.resourceCompleted) {
			Thread.sleep(250);
		}
		
	}
	
	private class TestRequestHandler implements HttpRequestHandler {

		public int reqCount = 0;
		public boolean resourceCreated = false;
		public boolean resourceCompleted = false;
		private String baseURL;
		
		public TestRequestHandler(String baseURL) {
			this.baseURL = baseURL;
		}
		
		@Override
		public boolean processRequest(SimpleHttpServerConnection conn,
				SimpleRequest request) throws IOException {
			try {
				
				if (request.getRequestLine().getUri().equals("/")) {
					assertTrue(request.getContentType().startsWith("application/atom+xml"));
					Document doc = DocumentHelper.parseText(request.getBodyString());
					assertEquals("entry", doc.getRootElement().getName());
					
					assertNotNull(doc.selectSingleNode("//atom:title"));
					assertNotNull(doc.selectSingleNode("//atom:id"));
					assertNotNull(doc.selectSingleNode("//atom:updated"));
					assertNotNull(doc.selectSingleNode("//atom:author/atom:name"));
					assertEquals(SwordSerializedBagWriter.PACKAGING, doc.selectSingleNode("//sword:packaging").getText());
					
					SimpleResponse response = new SimpleResponse();
					response.setStatusLine(HttpVersion.HTTP_1_1, 201, "Resource created");
					doc.getRootElement().addElement("atom:link")
							.addAttribute("rel", "edit")
							.addAttribute("href", baseURL + "edit");
					response.setBodyString(doc.asXML());
					conn.writeResponse(response);
					resourceCreated = true;
				} else if (request.getRequestLine().getMethod().equalsIgnoreCase("POST")) {
					assertTrue(resourceCreated);
					assertFalse(resourceCompleted);
					reqCount++;
					SimpleResponse response = new SimpleResponse();
					response.setStatusLine(HttpVersion.HTTP_1_1, 204, "No content");
					conn.writeResponse(response);					
				} else {
					assertEquals(9, reqCount);
					assertFalse(resourceCompleted);
					Document doc = DocumentHelper.parseText(request.getBodyString());
					assertEquals("entry", doc.getRootElement().getName());
					
					assertNotNull(doc.selectSingleNode("//bob:completed"));
					SimpleResponse response = new SimpleResponse();
					response.setStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
					response.setBodyString(doc.asXML());
					conn.writeResponse(response);
					
					resourceCompleted = true;					
				}
				
				return true;

			}
			catch(Exception ex) {
				throw new RuntimeException(ex);
			}			
			
		}
		
	}

}
