package gov.loc.repository.bagit.transfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.RelaxedSSLProtocolSocketFactory;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import gov.loc.repository.bagit.Manifest.Algorithm;

public class SwordSender {

	public static final String CONTENT_TYPE = "application/zip";
	public static final String PACKAGING = "http://purl.org/net/sword-types/bagit";

	private static final Log log = LogFactory.getLog(SwordSender.class);

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private ZipWriter zipWriter = null;
	private Integer statusCode = null;
	private String body = null;
	private String location = null;
	private boolean relaxedSSL = false;
	private String username;
	private String password;
	
	public SwordSender(ZipWriter zipWriter) {		
		this.zipWriter = zipWriter;
	}
	
	public void setBagDir(String bagDir) {
		this.zipWriter.setBagDir(bagDir);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public void setRelaxedSSL(boolean relaxedSSL) {
		this.relaxedSSL = relaxedSSL;
	}

	@SuppressWarnings("deprecation")
	public void send(Bag bag, String collectionURL) {
		this.out = new ByteArrayOutputStream();
		this.zipWriter.write(bag, this.out);
		
		//This allows self-signed certs
		if (relaxedSSL) {
			Protocol relaxedHttps = new Protocol("https", new RelaxedSSLProtocolSocketFactory(), 443);
			Protocol.registerProtocol("https", relaxedHttps);
		}
				
		HttpClient client = new HttpClient();
		
		if (this.username != null) {
			client.getParams().setAuthenticationPreemptive(true);
			Credentials creds = new UsernamePasswordCredentials(this.username, this.password);
			try {
				URL url = new URL(collectionURL);
				client.getState().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		}
				
		PostMethod post = new PostMethod(collectionURL);
		post.addRequestHeader("X-Packaging", PACKAGING);
		byte[] bagBytes = this.out.toByteArray();
		post.addRequestHeader("Content-MD5", MessageDigestHelper.generateFixity(new ByteArrayInputStream(bagBytes), Algorithm.MD5 ));
		post.setRequestEntity(new ByteArrayRequestEntity(bagBytes, CONTENT_TYPE));
		try {
			log.debug("Posting to " + collectionURL);
			client.executeMethod(post);
			log.debug(MessageFormat.format("Response to post was response code {0} and response body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
			this.statusCode = post.getStatusCode();
			this.body = post.getResponseBodyAsString();
			Header locationHeader = post.getResponseHeader("Location");
			if (locationHeader != null) {
				this.location = locationHeader.getValue();
				log.info("Location is " + this.location);				
			}
			if (post.getStatusCode() != HttpStatus.SC_CREATED) {
				throw new RuntimeException(MessageFormat.format("Attempt to create resource failed.  Server returned a response code of {0} and body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			post.releaseConnection();
		}

	}
	
	public String getLocation() {
		return location;
	}
	
	public Integer getStatusCode() {
		return statusCode;
	}

	public String getBody() {
		return body;
	}

}
