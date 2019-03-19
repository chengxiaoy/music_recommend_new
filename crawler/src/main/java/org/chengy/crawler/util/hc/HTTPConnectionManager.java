package org.chengy.crawler.util.hc;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by nali on 2017/9/12.
 */
public class HTTPConnectionManager {


	private static PoolingHttpClientConnectionManager connectionManager;


	static {
		SSLContextBuilder builder = new SSLContextBuilder();
		SSLConnectionSocketFactory sslsf=null;
		try {
			builder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
					return true;
				}
			});
			sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

		}catch (Exception e){
			System.out.println("error setting sslsf in generate httpclient");
		}

		Registry<ConnectionSocketFactory> reg = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https",
						sslsf).build();

		connectionManager = new PoolingHttpClientConnectionManager(reg);

		connectionManager.setDefaultMaxPerRoute(200);
		connectionManager.setMaxTotal(200);


	}

	public static HttpClientConnectionManager getConnectionManager() {
		return connectionManager;
	}
}
