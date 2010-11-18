package gov.loc.repository.bagit.utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;


public class RelaxedSSLProtocolSocketFactory implements SecureProtocolSocketFactory, ProtocolSocketFactory {

	
		private SSLContext sslContext = null;
		    
	    /**
	     * Constructor for SSLProtocolSocketFactory.
	     */
	    public RelaxedSSLProtocolSocketFactory() {
	        try {
		    	this.sslContext = SSLContext.getInstance("SSL");
		        this.sslContext.init(
		                null, 
		                new TrustManager[] {new RelaxedX509TrustManager(null)}, 
		                null);
	        } catch (Exception ex) {
	        	throw new HttpClientError(ex.toString());
	        }
	    }

	    /**
	     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
	     */
	    public Socket createSocket(
	        String host,
	        int port,
	        InetAddress clientHost,
	        int clientPort)
	        throws IOException, UnknownHostException {
	    	return this.sslContext.getSocketFactory().createSocket(
	                host,
	                port,
	                clientHost,
	                clientPort

	        );
	    }

	    /**
	     * Attempts to get a new socket connection to the given host within the given time limit.
	     * <p>
	     * This method employs several techniques to circumvent the limitations of older JREs that 
	     * do not support connect timeout. When running in JRE 1.4 or above reflection is used to 
	     * call Socket#connect(SocketAddress endpoint, int timeout) method. When executing in older 
	     * JREs a controller thread is executed. The controller thread attempts to create a new socket
	     * within the given limit of time. If socket constructor does not return until the timeout 
	     * expires, the controller terminates and throws an {@link ConnectTimeoutException}
	     * </p>
	     *  
	     * @param host the host name/IP
	     * @param port the port on the host
	     * @param localAddress the local host name/IP to bind the socket to
	     * @param localPort the port on the local machine
	     * @param params {@link HttpConnectionParams Http connection parameters}
	     * 
	     * @return Socket a new socket
	     * 
	     * @throws IOException if an I/O error occurs while creating the socket
	     * @throws UnknownHostException if the IP address of the host cannot be
	     * determined
	     * 
	     * @since 3.0
	     */
	    public Socket createSocket(
	        final String host,
	        final int port,
	        final InetAddress localAddress,
	        final int localPort,
	        final HttpConnectionParams params
	    ) throws IOException, UnknownHostException, ConnectTimeoutException {
	        if (params == null) {
	            throw new IllegalArgumentException("Parameters may not be null");
	        }
	        int timeout = params.getConnectionTimeout();
	        if (timeout == 0) {
	            return createSocket(host, port, localAddress, localPort);
	        } else {
	        	Socket socket = this.sslContext.getSocketFactory().createSocket();
	            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
	            SocketAddress remoteaddr = new InetSocketAddress(host, port);
	            socket.bind(localaddr);
	            socket.connect(remoteaddr, timeout);
	            return socket;
	        }
	    }

	    /**
	     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
	     */
	    public Socket createSocket(String host, int port)
	        throws IOException, UnknownHostException {
	        return this.sslContext.getSocketFactory().createSocket(
	            host,
	            port
	        );
	    }

	    /**
	     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	     */
	    public Socket createSocket(
	        Socket socket,
	        String host,
	        int port,
	        boolean autoClose)
	        throws IOException, UnknownHostException {
	        return this.sslContext.getSocketFactory().createSocket(
	            socket,
	            host,
	            port,
	            autoClose
	        );
	    }

	    /**
	     * All instances of SSLProtocolSocketFactory are the same.
	     */
	    @Override
	    public boolean equals(Object obj) {
	        return ((obj != null) && obj.getClass().equals(getClass()));
	    }

	    /**
	     * All instances of SSLProtocolSocketFactory have the same hash code.
	     */
	    @Override
	    public int hashCode() {
	        return getClass().hashCode();
	    } 
}
