package org.chengy.crawler.util.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.util.ProxyIPPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class VertxClientFactory {
	@Autowired
	private ProxyIPPoolUtil proxyIPPoolUtil;

	private Vertx vertx = Vertx.vertx();


	public WebClient newWebClientWithProxy(String host,int port){
		WebClientOptions webClientOptions = new WebClientOptions();
		webClientOptions.setMaxPoolSize(10).setConnectTimeout(1000).setKeepAlive(true)
				.setDefaultHost("music.163.com");
		webClientOptions.setProxyOptions(new ProxyOptions().setHost(host).setPort(port));
		return WebClient.create(vertx, webClientOptions);
	}

	public WebClient newWebClientWithProxy() {
		Pair<String, Integer> pair = proxyIPPoolUtil.peekIp();
		WebClientOptions webClientOptions = new WebClientOptions();
		webClientOptions.setMaxPoolSize(10).setConnectTimeout(1000).setKeepAlive(true)
				.setDefaultHost("music.163.com");
		webClientOptions.setProxyOptions(new ProxyOptions().setHost(pair.getLeft()).setPort(pair.getRight()));
		return WebClient.create(vertx, webClientOptions);
	}

	public WebClient newWebClient() {
		return newWebClient(10, 1000);
	}

	public WebClient newWebClient(int poolSize, int timeout) {
		WebClientOptions webClientOptions = new WebClientOptions();
		webClientOptions.setMaxPoolSize(poolSize).setConnectTimeout(timeout).setKeepAlive(true)
				.setDefaultHost("music.163.com");
		return WebClient.create(vertx, webClientOptions);
	}

}
