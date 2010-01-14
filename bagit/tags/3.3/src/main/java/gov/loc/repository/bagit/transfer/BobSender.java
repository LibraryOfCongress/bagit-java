package gov.loc.repository.bagit.transfer;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.utilities.RelaxedSSLProtocolSocketFactory;

public class BobSender {

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public static final String SWORD_NAMESPACE = "http://purl.org/net/sword";
	public static final String BOB_NAMESPACE = "http://repository.loc.gov/bob/";

	private static final Log log = LogFactory.getLog(BobSender.class);

	
	private boolean relaxedSSL = false;
	private int threads = 0;
	private String username;
	private String password;
	private int throttle = 0;

	public BobSender() {
		this.threads = Runtime.getRuntime().availableProcessors();
	}
	
	public void send(Bag bag, String collectionURL) {
		bag.accept(new BobVisitor(collectionURL, this.relaxedSSL, this.username, this.password, this.throttle, this.threads));
	}
	
	public void setRelaxedSSL(boolean relaxedSSL) {
		this.relaxedSSL = relaxedSSL;
	}
	
	public void setThreads(int threads) {
		this.threads = threads;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setThrottle(int throttle) {
		this.throttle = throttle;
	}
	private class BobVisitor extends AbstractBagVisitor {
					
		private String collectionURL;
		private boolean relaxedSSL = false;
		private String title = null;
		private String id = null;
		private String authorName = null;
		private String authorEmail = null;
		private String content = null;
		private String editURL = null;
		private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		private Integer threads = 0;
		private ThreadPoolExecutor executor;
		private Document atomDoc;
		private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");	
		private String username;
		private String password;
		private int throttle = 0;
		
		public BobVisitor(String collectionURL, boolean relaxedSSL, String username, String password, int throttle, int threads) {
			this.collectionURL = collectionURL;
			this.relaxedSSL = relaxedSSL;
			this.username = username;
			this.password = password;
			this.throttle = throttle;
			this.threads = threads;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public void setAuthorName(String authorName) {
			this.authorName = authorName;
		}
		
		public void setAuthorEmail(String authorEmail) {
			this.authorEmail = authorEmail;
		}
		
		public void setContent(String content) {
			this.content = content;
		}
		
		@SuppressWarnings("deprecation")
		public void setThreads(int threads) {
			this.threads = threads;
			this.connectionManager.setMaxConnectionsPerHost(this.threads + 1);
			this.connectionManager.setMaxTotalConnections(this.threads + 1);		
		}
			
		@Override
		public void endBag() {
			this.executor.shutdown();
			while(! this.executor.isTerminated()) {
				this.wait(250);
			}
			atomDoc.getRootElement().addElement("bob:completed")
					.addText(df.format(new Date()));
			PutMethod put = new PutMethod(editURL);
			try {
				put.setRequestEntity(new ByteArrayRequestEntity(atomDoc.asXML().getBytes("utf-8"), "application/atom+xml; charset=\"utf-8\""));
				log.debug(MessageFormat.format("Putting to {0}.  Message body is: {1}", collectionURL, atomDoc.asXML()));
				this.executeMethod(put);
				log.debug(MessageFormat.format("Response to put was response code {0} and response body of {1}", put.getStatusCode(), put.getResponseBodyAsString()));
				if (put.getStatusCode() != HttpStatus.SC_OK) {
					throw new RuntimeException(MessageFormat.format("Attempt to update resource failed.  Server returned a response code of {0} and body of {1}", put.getStatusCode(), put.getResponseBodyAsString()));
				}			
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				put.releaseConnection();
			}
	
		}
	
		private void executeMethod(HttpMethod method) throws HttpException, IOException {
			HttpClient client = this.getClient();
			client.executeMethod(method);
			this.wait(this.throttle);
		}
		
		private void wait(int length) {
			try {
				Thread.sleep(length);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
	
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void startBag(Bag bag) {
			//Set entry values
			if (this.title == null) {
				this.title = "Untitled bag";
				if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getExternalIdentifier() != null) {
					this.title = bag.getBagInfoTxt().getExternalIdentifier();
				}
			}
			
			if (this.id == null) {
				this.id = "Unidentified bag";
				if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getExternalIdentifier() != null) {
					this.id = bag.getBagInfoTxt().getExternalIdentifier();
				}
			}
			
			if (this.authorName == null) {
				//Try to use contact
				//If not, try source organization
				//Otherwise, default to unauthored
				this.authorName = "Unauthored bag";
				if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getContactName() != null) {
					this.authorName = bag.getBagInfoTxt().getContactName();
				} else if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getSourceOrganization() != null) {
					this.authorName = bag.getBagInfoTxt().getSourceOrganization();
				}
			}
			
			if (this.authorEmail == null) {
				if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getContactEmail() != null) {
					this.authorEmail = bag.getBagInfoTxt().getContactEmail();
				}
			}
	
			if (this.content == null) {
				this.content = "No content.";
				if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getExternalDescription() != null) {
					this.content = bag.getBagInfoTxt().getExternalDescription();
				}
			}
					
			atomDoc = DocumentHelper.createDocument();
			Element entryElem = atomDoc.addElement("atom:entry", ATOM_NAMESPACE)
					.addNamespace("sword", SWORD_NAMESPACE)
					.addNamespace("bob", BOB_NAMESPACE);
			entryElem.addElement("atom:title")
					.addText(this.title);
			entryElem.addElement("atom:id")
					.addText(this.id);
			entryElem.addElement("atom:updated")
					.addText(df.format(new Date()));
			Element authorElem = entryElem.addElement("atom:author");
			authorElem.addElement("atom:name")
					.addText(this.authorName);
			if (this.authorEmail != null) {
				authorElem.addElement("atom:email")
						.addText(this.authorEmail);
			}
			entryElem.addElement("sword:packaging")
					.addText(SwordSender.PACKAGING);
			if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getPayloadOxum() != null) {
				entryElem.addElement("bob:oxum")
						.addText(bag.getBagInfoTxt().getPayloadOxum());
			}
			entryElem.addElement("atom:content")
					.addText(this.content);
	
			//This allows self-signed certs
			if (relaxedSSL) {
				Protocol relaxedHttps = new Protocol("https", new RelaxedSSLProtocolSocketFactory(), 443);
				Protocol.registerProtocol("https", relaxedHttps);
			}		
			
			PostMethod post = new PostMethod(collectionURL);
			try {
				post.setRequestEntity(new ByteArrayRequestEntity(atomDoc.asXML().getBytes("utf-8"), "application/atom+xml; charset=\"utf-8\""));
				log.debug(MessageFormat.format("Posting to {0}.  Message body is: {1}", collectionURL, atomDoc.asXML()));
				this.executeMethod(post);
				log.debug(MessageFormat.format("Response to post was response code {0} and response body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
				if (post.getStatusCode() != HttpStatus.SC_CREATED) {
					throw new RuntimeException(MessageFormat.format("Attempt to create resource failed.  Server returned a response code of {0} and body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
				}
	
				//Get the edit link
				atomDoc = DocumentHelper.parseText(post.getResponseBodyAsString());
				Element linkElem = (Element)atomDoc.getRootElement().selectSingleNode("//atom:link[@rel='edit']");
				if (linkElem == null) {
					throw new BagTransferException("Response Atom document does not contain edit link.");
				}
				this.editURL = linkElem.attributeValue("href");
				
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				post.releaseConnection();
			}
			this.executor = new ThreadPoolExecutor(this.threads == 1 ? 1 : 2, this.threads, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
		}
	
		@Override
		public void visitPayload(BagFile bagFile) {
			this.visitBagFile(bagFile);
		}
		
		@Override
		public void visitTag(BagFile bagFile) {
			this.visitBagFile(bagFile);		
		}
			
		private void visitBagFile(BagFile bagFile) {
			this.executor.execute(new PostResourceRunnable(bagFile.getFilepath(), bagFile));
		}
		
		private HttpClient getClient() {
			HttpClient client = new HttpClient(this.connectionManager);
			
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
	
			return client;
		}
	
		private class PostResourceRunnable implements Runnable {
			
			private String filepath;
			private BagFile bagFile;
			
			public PostResourceRunnable(String filepath, BagFile bagFile) {
				this.filepath = filepath;
				this.bagFile = bagFile;			
			}
			
			@Override
			public void run() {
				PostMethod post = new PostMethod(editURL);
				post.addRequestHeader("Content-Disposition", "attachment; filename=" + this.filepath);
				post.setRequestEntity(new InputStreamRequestEntity(this.bagFile.newInputStream(), "application/octet-stream"));
				try {
					log.debug(MessageFormat.format("Posting {0} to {1}", this.filepath, editURL));
					executeMethod(post);
					log.debug(MessageFormat.format("Response to post was response code {0} and response body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
					if (post.getStatusCode() != HttpStatus.SC_CREATED && post.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
						throw new RuntimeException(MessageFormat.format("Attempt to create resource failed.  Server returned a response code of {0} and body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				} finally {
					post.releaseConnection();
				}
				
			}
			
		}
	}
		
}
