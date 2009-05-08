package gov.loc.repository.bagit.transfer;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transfer.SwordSender;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.impl.ZipWriter;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SwordSenderTest {

	SimpleHttpServer server;
	BagFactory bagFactory = new BagFactory();
	
	@Before
	public void setUp() throws Exception {
		this.server = new SimpleHttpServer();
	}
	
	@After
	public void teardown() throws Exception {
		this.server.destroy();
	}
	
	@Test
	public void testVisitor() throws Exception {
		this.server.setRequestHandler(new TestRequestHandler());
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		assertTrue(bag.checkValid().isSuccess());

		SwordSender visitor = new SwordSender(new ZipWriter(this.bagFactory));
		visitor.setBagDir("test_bag");
		visitor.send(bag, "http://localhost:" + this.server.getLocalPort() + "/");
		
		assertEquals(Integer.valueOf(201), visitor.getStatusCode());
		assertEquals("http://localhost/foo.atom", visitor.getLocation());
		assertEquals("<A Atom Media Entry Document>", visitor.getBody());
		
	}
	
	private class TestRequestHandler implements HttpRequestHandler {

		@Override
		public boolean processRequest(SimpleHttpServerConnection conn,
				SimpleRequest request) throws IOException {
			System.out.println(request.getContentType());
			assertEquals("application/zip", request.getContentType());
			assertTrue(request.containsHeader("X-Packaging"));
			assertTrue(request.containsHeader("Content-MD5"));
			MessageDigestHelper.fixityMatches(request.getBody(), Algorithm.MD5, request.getFirstHeader("Content-MD5").getValue());
			SimpleResponse response = new SimpleResponse();
			response.setStatusLine(HttpVersion.HTTP_1_1, 201, "Resource created");
			response.addHeader(new Header("Location", "http://localhost/foo.atom"));
			response.setBodyString("<A Atom Media Entry Document>");
			conn.writeResponse(response);
			return true;
			
		}
		
	}

}
