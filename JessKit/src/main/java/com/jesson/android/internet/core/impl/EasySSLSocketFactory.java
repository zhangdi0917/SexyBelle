package com.jesson.android.internet.core.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 信任所有安全证书， 根据apache SSLSocketFactory修改
* @author yingjun.bai@renren-inc.com  2012-12-18 上午11:59:22
 */
public class EasySSLSocketFactory implements LayeredSocketFactory {

	public static final String TLS = "TLS";
	public static final String SSL = "SSL";
	public static final String SSLV2 = "SSLv2";

	public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();

	public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = new BrowserCompatHostnameVerifier();

	public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifier();
	/**
	 * The factory using the default JVM settings for secure connections.
	 */
	private static EasySSLSocketFactory DEFAULT_FACTORY;

	/**
	 * Gets an singleton instance of the SSLProtocolSocketFactory.
	 * 
	 * @return a SSLProtocolSocketFactory
	 */
	public static EasySSLSocketFactory getSocketFactory() {
		if (DEFAULT_FACTORY == null)
			try {
				DEFAULT_FACTORY = new EasySSLSocketFactory();
			} catch (KeyManagementException e) {
			} catch (NoSuchAlgorithmException e) {
			}
		return DEFAULT_FACTORY;
	}

	private final SSLContext sslcontext;
	private final javax.net.ssl.SSLSocketFactory socketfactory;
	private final HostNameResolver nameResolver;
	private X509HostnameVerifier hostnameVerifier = ALLOW_ALL_HOSTNAME_VERIFIER;

	public EasySSLSocketFactory(String algorithm, final KeyStore keystore,
			final String keystorePassword, final KeyStore truststore,
			final SecureRandom random, final HostNameResolver nameResolver)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super();
		if (algorithm == null) {
			algorithm = TLS;
		}
		KeyManager[] keymanagers = null;
		if (keystore != null) {
			keymanagers = createKeyManagers(keystore, keystorePassword);
		}
		TrustManager[] trustmanagers = null;
		if (truststore != null) {
			trustmanagers = createTrustManagers(truststore);
		}
		this.sslcontext = SSLContext.getInstance(algorithm);
		this.sslcontext.init(keymanagers, trustmanagers, random);
		this.socketfactory = this.sslcontext.getSocketFactory();
		this.nameResolver = nameResolver;
	}

	public EasySSLSocketFactory(final KeyStore keystore,
			final String keystorePassword, final KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		this(TLS, keystore, keystorePassword, truststore, null, null);
	}

	public EasySSLSocketFactory(final KeyStore keystore,
			final String keystorePassword) throws NoSuchAlgorithmException,
			KeyManagementException, KeyStoreException,
			UnrecoverableKeyException {
		this(TLS, keystore, keystorePassword, null, null, null);
	}

	public EasySSLSocketFactory(final KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		this(TLS, null, null, truststore, null, null);
	}

	/**
	 * Constructs an HttpClient SSLSocketFactory backed by the given JSSE
	 * SSLSocketFactory.
	 * 
	 * @hide
	 */
	public EasySSLSocketFactory(javax.net.ssl.SSLSocketFactory socketfactory) {
		super();
		this.sslcontext = null;
		this.socketfactory = socketfactory;
		this.nameResolver = null;
	}

	/**
	 * Creates the default SSL socket factory. This constructor is used
	 * exclusively to instantiate the factory for {@link #getSocketFactory
	 * getSocketFactory}.
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private EasySSLSocketFactory() throws NoSuchAlgorithmException,
			KeyManagementException {
		super();
		// this.sslcontext = null;
		// this.socketfactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		// this.nameResolver = null;
		this.sslcontext = SSLContext.getInstance(TLS);
		this.sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} }, new SecureRandom());
		this.socketfactory = this.sslcontext.getSocketFactory();
		this.nameResolver = null;
	}

	private static KeyManager[] createKeyManagers(final KeyStore keystore,
			final String password) throws KeyStoreException,
			NoSuchAlgorithmException, UnrecoverableKeyException {
		if (keystore == null) {
			throw new IllegalArgumentException("Keystore may not be null");
		}
		KeyManagerFactory kmfactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmfactory.init(keystore, password != null ? password.toCharArray()
				: null);
		return kmfactory.getKeyManagers();
	}

	private static TrustManager[] createTrustManagers(final KeyStore keystore)
			throws KeyStoreException, NoSuchAlgorithmException {
		if (keystore == null) {
			throw new IllegalArgumentException("Keystore may not be null");
		}
		TrustManagerFactory tmfactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmfactory.init(keystore);
		return tmfactory.getTrustManagers();
	}

	// non-javadoc, see interfaces org.apache.http.conn.SocketFactory
	public Socket createSocket() throws IOException {

		// the cast makes sure that the factory is working as expected
		return (SSLSocket) this.socketfactory.createSocket();
	}

	// non-javadoc, see interfaces org.apache.http.conn.SocketFactory
	public Socket connectSocket(final Socket sock, final String host,
			final int port, final InetAddress localAddress, int localPort,
			final HttpParams params) throws IOException {

		if (host == null) {
			throw new IllegalArgumentException("Target host may not be null.");
		}
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null.");
		}

		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		if ((localAddress != null) || (localPort > 0)) {

			// we need to bind explicitly
			if (localPort < 0)
				localPort = 0; // indicates "any"

			InetSocketAddress isa = new InetSocketAddress(localAddress,
					localPort);
			sslsock.bind(isa);
		}

		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);

		InetSocketAddress remoteAddress;
		if (this.nameResolver != null) {
			remoteAddress = new InetSocketAddress(
					this.nameResolver.resolve(host), port);
		} else {
			remoteAddress = new InetSocketAddress(host, port);
		}

		sslsock.connect(remoteAddress, connTimeout);

		sslsock.setSoTimeout(soTimeout);
		try {
			hostnameVerifier.verify(host, sslsock);
			// verifyHostName() didn't blowup - good!
		} catch (IOException iox) {
			// close the socket before re-throwing the exception
			try {
				sslsock.close();
			} catch (Exception x) { /* ignore */
			}
			throw iox;
		}

		return sslsock;
	}

	/**
	 * Checks whether a socket connection is secure. This factory creates
	 * TLS/SSL socket connections which, by default, are considered secure. <br/>
	 * Derived classes may override this method to perform runtime checks, for
	 * example based on the cypher suite.
	 * 
	 * @param sock
	 *            the connected socket
	 * 
	 * @return <code>true</code>
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument is invalid
	 */
	public boolean isSecure(Socket sock) throws IllegalArgumentException {

		if (sock == null) {
			throw new IllegalArgumentException("Socket may not be null.");
		}
		// This instanceof check is in line with createSocket() above.
		if (!(sock instanceof SSLSocket)) {
			throw new IllegalArgumentException(
					"Socket not created by this factory.");
		}
		// This check is performed last since it calls the argument object.
		if (sock.isClosed()) {
			throw new IllegalArgumentException("Socket is closed.");
		}

		return true;

	} // isSecure

	// non-javadoc, see interfaces LayeredSocketFactory
	public Socket createSocket(final Socket socket, final String host,
			final int port, final boolean autoClose) throws IOException,
			UnknownHostException {
		SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(
				socket, host, port, autoClose);
		hostnameVerifier.verify(host, sslSocket);
		// verifyHostName() didn't blowup - good!
		return sslSocket;
	}

	public void setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {
		if (hostnameVerifier == null) {
			throw new IllegalArgumentException(
					"Hostname verifier may not be null");
		}
		this.hostnameVerifier = hostnameVerifier;
	}

	public X509HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

}