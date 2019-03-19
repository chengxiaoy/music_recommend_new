package org.chengy.crawler.website.neteastmusic;

import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.model.Music163Song;
import org.chengy.crawler.model.Music163User;
import org.chengy.crawler.repository.Music163SongRepository;
import org.chengy.crawler.repository.Music163UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Component
public class NetEastMusicFacade {


	private static final Logger LOGGER = LoggerFactory.getLogger(NetEastMusicFacade.class);

	@Autowired
	Music163UserRepository userRepository;
	@Autowired
	Music163SongRepository songRepository;

	@Autowired
	NetEastClient netEastClient;

	private ExecutorService userExecutor =
			new ThreadPoolExecutor(5, 5, 100L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());

	private ExecutorService songExecutor =
			new ThreadPoolExecutor(5, 5, 100L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());


	@Autowired
	CrawlerBizConfig crawlerBizConfig;

	@Autowired
	NetEastFilter filter;


	public void crawlM163Songs() {


		while (true){
			String songId= crawlerBizConfig.getCrawlerSongid();
			if (filter.containsSongId(songId)){
				continue;
			}

			try {
				netEastClient.getSongInfo(songId);
			} catch (Exception e) {
				LOGGER.error("craw song {} failed", songId, e);
			}


		}

	}


	public void crawlM163User() {

		while (true) {
			String uid = crawlerBizConfig.getCrawlerUid();
			try {
				boolean userExit = filter.containsUid(uid);
				if (userExit && !crawlerBizConfig.needAdd()) {
					continue;
				}

				if (!crawlerBizConfig.isCrawlUserSwitch()) {
					Thread.sleep(1000 * 10);
				}

				Runnable crawlerUserInfoTask = new Runnable() {
					@Override
					public void run() {
						boolean flag = crawlerBizConfig.needAdd();
						CompletableFuture<CrawlerUserInfo> crawlerInfoFuture = netEastClient.getUserInfoAsync(uid, flag, userExit);

						crawlerInfoFuture.whenComplete((crawlerInfo, throwable) -> {
							if (throwable != null) {
								LOGGER.warn("craw user {} failed", uid, throwable);
							}
							List<String> relativeIds = crawlerInfo.getRelativeIds();
							if (!org.apache.commons.collections.CollectionUtils.isEmpty(relativeIds)) {
								crawlerBizConfig.setCrawlUids(relativeIds);
							}
							if (crawlerInfo.getUser() != null && !userExit) {
								Music163User user = crawlerInfo.getUser();
								List<Pair<String, Integer>> songInfo = crawlerInfo.getLoveSongs();

								List<String> songIds = songInfo.stream()
										.map(Pair::getLeft).collect(Collectors.toList());
								crawlerBizConfig.setSongIds(songIds);
								user.setLoveSongId(songIds);
								user.setSongScore(songInfo);
								userRepository.save(user);
								filter.putUid(uid);
								LOGGER.info("craw user" + uid + " succeed!");
							}
						});
					}
				};
				userExecutor.execute(crawlerUserInfoTask);
			} catch (Exception e) {
				System.out.println(uid + " get info failed");
				e.printStackTrace();
			}
		}
	}

}
