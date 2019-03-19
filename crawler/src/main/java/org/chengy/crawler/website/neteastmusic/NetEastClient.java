package org.chengy.crawler.website.neteastmusic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.model.Music163Song;
import org.chengy.crawler.model.Music163User;
import org.chengy.crawler.repository.Music163SongRepository;
import org.chengy.crawler.repository.Music163UserRepository;
import org.chengy.crawler.util.vertx.VertxClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Component
public class NetEastClient {
	public static Logger LOGGER = LoggerFactory.getLogger(NetEastClient.class);


	@Autowired
	private Music163UserRepository userRepository;

	@Autowired
	private Music163SongRepository songRepository;

	@Autowired
	NetEastParser m163Parser;

	@Autowired
	VertxClientFactory vertxClientFactory;

	private ThreadLocal<WebClient> clientThreadLocal = ThreadLocal.withInitial(() -> {
		return vertxClientFactory.newWebClient();
	});

	ExecutorService parseExecutorService = Executors.newFixedThreadPool(5);


	public CompletableFuture<CrawlerUserInfo> getUserInfoAsync(String uid, boolean relativeUser, boolean userExit) {
		CompletableFuture<List<String>> relativeUsers = new CompletableFuture<>();
		relativeUsers.complete(new ArrayList<>());
		// 获取用户的相关用户
		if (relativeUser) {
			relativeUsers = getRelativeUserIds(uid);
		}
		// 如果用户已经被记录, 则直接返回
		if (userExit) {
			CrawlerUserInfo res = new CrawlerUserInfo(null, null);
			return relativeUsers.thenApplyAsync((relatives) -> {
				res.setRelativeIds(relatives);
				return res;
			});
		}
		CompletableFuture<String> htmlFuture = getHtml(NetEastApiCons.userHost + uid);
		CompletableFuture<Music163User> m163UserFuture = htmlFuture.thenApplyAsync(html -> m163Parser.parseUser(html, uid), parseExecutorService);


		CompletableFuture<List<Pair<String, Integer>>> loveSongsFuture = getLoveSongs(uid);

		CompletableFuture<CrawlerUserInfo> crawlerUserInfoFuture = m163UserFuture.thenCombineAsync(relativeUsers, CrawlerUserInfo::new);
		return crawlerUserInfoFuture.thenCombine(loveSongsFuture, (userInfo, songScores) -> {
			userInfo.setLoveSongs(songScores);
			return userInfo;
		});

	}

	/**
	 * 获取用户相关的用户
	 *
	 * @param uid
	 * @return
	 */
	private CompletableFuture<List<String>> getRelativeUserIds(String uid) {
		// todo
		CompletableFuture<List<String>> completableFuture = new CompletableFuture<>();
		try {
			String fansParam = NetEastApiCons.getFansParams(uid, 1, 100);
			CompletableFuture<String> fansFutureJsonStr = EncryptTools.commentAPIAsync(clientThreadLocal.get(), fansParam, NetEastApiCons.fansUrl);
			ObjectMapper objectMapper = new ObjectMapper();

			AtomicInteger steps = new AtomicInteger(0);
			List<String> relativeIds = new ArrayList<>();

			fansFutureJsonStr.whenCompleteAsync((jsonStr, t) -> {
						if (t != null) {
							completableFuture.completeExceptionally(t.getCause());
						} else {
							try {
								JsonNode root = objectMapper.readTree(jsonStr);
								List<JsonNode> jsonNodeList =
										root.findValue("followeds").findValues("userId");
								List<String> ids =
										jsonNodeList.stream().map(JsonNode::asText).collect(Collectors.toList());
								relativeIds.addAll(ids);
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								if (steps.getAndIncrement() == 0) {
									completableFuture.complete(relativeIds);
								}
							}
						}

					}
			);

			return completableFuture;
		} catch (Exception e) {
			e.printStackTrace();
			completableFuture.completeExceptionally(e);
		}
		return completableFuture;
	}


	/**
	 * 获取用户喜爱的歌曲
	 *
	 * @param uid
	 * @return
	 */
	public CompletableFuture<List<Pair<String, Integer>>> getLoveSongs(String uid) {
		CompletableFuture<List<Pair<String, Integer>>> res = new CompletableFuture<>();
		try {
			String songRecordParam = NetEastApiCons.getSongRecordALLParams(uid, 1, 100);
			CompletableFuture<String> songJsonStr = EncryptTools.commentAPIAsync(clientThreadLocal.get(), songRecordParam, NetEastApiCons.songRecordUrl);
			songJsonStr.whenCompleteAsync((jsonStr, t) -> {
				if (t != null) {
					res.completeExceptionally(t.getCause());
				} else {
					List<Pair<String, Integer>> pairList = m163Parser.userLoveSongScores(uid, jsonStr);
					res.complete(pairList);
				}
			});
		} catch (Exception e) {
			LOGGER.info("get like song failed:" + uid, e);
		}
		return res;
	}

	/**
	 * 获取 absurl的静态网页
	 *
	 * @param absUrl
	 * @return
	 */
	private CompletableFuture<String> getHtml(String absUrl) {
		WebClient webClient = clientThreadLocal.get();
		HttpRequest<Buffer> request = webClient.requestAbs(HttpMethod.GET, absUrl);
		CompletableFuture<String> futureHtml = new CompletableFuture<>();
		request.send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> response = ar.result();
				if (response.statusCode() == 503) {
					futureHtml.completeExceptionally(new IllegalStateException("ip has been temporarily bloked"));
				}
				if (response.statusCode() == 200) {
					String html = response.body().toString(StandardCharsets.UTF_8);

					futureHtml.complete(html);
				} else {
					futureHtml.completeExceptionally(new IllegalStateException("http response is " + response.statusCode()));
				}
			} else if (ar.failed()) {
				futureHtml.completeExceptionally(ar.cause());
			}
		});


		return futureHtml;


	}


	public void getSongInfo(String songId) throws Exception {

		Music163Song exitSong = songRepository.findById(songId).orElse(null);
		if (exitSong != null) {
			return;
		}
		CompletableFuture<String> html =
				getHtml(NetEastApiCons.songHostUrl + songId);
		String params = NetEastApiCons.getLyricParams(songId);
		String lyricUrl = NetEastApiCons.lyricUrl;

		CompletableFuture<String> lyric = EncryptTools.commentAPIAsync(clientThreadLocal.get(), params, lyricUrl);


		html.thenCombine(lyric, (h, l) -> {
			try {
				saveSongInfo(h, songId, l);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		});

	}

	public void saveSongInfo(String html, String songId, String lyric) throws IOException {
		Music163Song song = m163Parser.parseSong(html, songId, lyric);

		songRepository.save(song);
	}


}
