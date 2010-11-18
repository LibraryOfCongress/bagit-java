package gov.loc.repository.bagit.bagwriter;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagWriter;

public class BobUnserializedBagWriter implements BagWriter {

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public static final String SWORD_NAMESPACE = "http://purl.org/net/sword";
	public static final String BOB_NAMESPACE = "http://repository.loc.gov/bob/";
	
	public static final Integer DEFAULT_THREADS = 2;
	
	private static final Log log = LogFactory.getLog(BobUnserializedBagWriter.class);

	
	private String collectionURL;
	private String title = null;
	private String id = null;
	private String authorName = null;
	private String authorEmail = null;
	private String content = null;
	private String editURL = null;
	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	private Integer threads = DEFAULT_THREADS;
	private ThreadPoolExecutor executor;
	private Document atomDoc;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");	
	
	public BobUnserializedBagWriter(String collectionURL) {
		this.collectionURL = collectionURL;
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
	public void close() {
		this.executor.shutdown();
		while(! this.executor.isTerminated()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		atomDoc.getRootElement().addElement("bob:completed")
				.addText(df.format(new Date()));
		HttpClient client = new HttpClient(this.connectionManager);
		PutMethod put = new PutMethod(editURL);
		try {
			put.setRequestEntity(new ByteArrayRequestEntity(atomDoc.asXML().getBytes("utf-8"), "application/atom+xml; charset=\"utf-8\""));
			log.debug(MessageFormat.format("Putting to {0}.  Message body is: {1}", collectionURL, atomDoc.asXML()));
			client.executeMethod(put);
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

	@Override
	public void open(Bag bag) {
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
				.addText(SwordSerializedBagWriter.PACKAGING);
		if (bag.getBagInfoTxt() != null && bag.getBagInfoTxt().getPayloadOxum() != null) {
			entryElem.addElement("bob:oxum")
					.addText(bag.getBagInfoTxt().getPayloadOxum());
		}
		entryElem.addElement("atom:content")
				.addText(this.content);
		
		HttpClient client = new HttpClient(this.connectionManager);
		PostMethod post = new PostMethod(collectionURL);
		try {
			post.setRequestEntity(new ByteArrayRequestEntity(atomDoc.asXML().getBytes("utf-8"), "application/atom+xml; charset=\"utf-8\""));
			log.debug(MessageFormat.format("Posting to {0}.  Message body is: {1}", collectionURL, atomDoc.asXML()));
			client.executeMethod(post);
			log.debug(MessageFormat.format("Response to post was response code {0} and response body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
			if (post.getStatusCode() != HttpStatus.SC_CREATED) {
				throw new RuntimeException(MessageFormat.format("Attempt to create resource failed.  Server returned a response code of {0} and body of {1}", post.getStatusCode(), post.getResponseBodyAsString()));
			}

			//Get the edit link
			atomDoc = DocumentHelper.parseText(post.getResponseBodyAsString());
			Element linkElem = (Element)atomDoc.getRootElement().selectSingleNode("//atom:link[@rel='edit']");
			if (linkElem == null) {
				throw new TransferException("Response Atom document does not contain edit link.");
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
	public void writePayloadFile(String filepath, BagFile bagFile) {
		this.writeFile(filepath, bagFile);
	}

	@Override
	public void writeTagFile(String filepath, BagFile bagFile) {
		this.writeFile(filepath, bagFile);
	}
	
	private void writeFile(String filepath, BagFile bagFile) {
		this.executor.execute(new PostResourceRunnable(filepath, bagFile));
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
			HttpClient client = new HttpClient(connectionManager);
			PostMethod post = new PostMethod(editURL);
			post.addRequestHeader("Content-Disposition", "attachment; filename=" + this.filepath);
			post.setRequestEntity(new InputStreamRequestEntity(this.bagFile.newInputStream(), "application/octet-stream"));
			try {
				log.debug(MessageFormat.format("Posting {0} to {1}", this.filepath, editURL));
				client.executeMethod(post);
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
