package org.chengy.crawler.website.neteastmusic;

import org.assertj.core.util.Lists;
import org.chengy.crawler.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class CrawlerBizConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerBizConfig.class);

	private static String crawlerUserSeed;

	private static List<String> m163userSeeds;
	private static String crawlerUserThreadNum;

	private static final String USER_KEY = "user_id";
	private static final String SONG_KEY = "song_id";

	private static final String USER_QUEUE = "user_queue";

	private static final String SONG_QUEUE = "song_queue";

	private boolean crawlUserSwitch = true;

	private boolean crawlSongSwitch = true;

	private ThreadLocal<Jedis> jedisThreadLocal = ThreadLocal.withInitial(RedisUtil::getJedis);

	public boolean isCrawlUserSwitch() {
		return crawlUserSwitch;
	}

	public void setCrawlUserSwitch(boolean crawlUserSwitch) {
		this.crawlUserSwitch = crawlUserSwitch;
	}

	@PostConstruct
	public void init() {
		try (Jedis jedis = RedisUtil.getJedis()) {
			Long queueSize = jedis.llen(USER_QUEUE);
			if (queueSize == null || queueSize == 0) {
				List<String> seedList = getCrawlerUserSeeds();
				String[] seeds = new String[seedList.size()];
				seedList.toArray(seeds);
				jedis.lpush(USER_QUEUE, seeds);
			}
			LOGGER.info("init user queue success");
		}
	}

	public String getCrawlerUid() {
		try (Jedis jedis = RedisUtil.getJedis()) {
			while (jedis.llen(USER_QUEUE) <= 0) {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return jedis.rpop(USER_QUEUE);
		}
	}

	public void setCrawlUids(List<String> uids) {

		try (Jedis jedis = RedisUtil.getJedis()) {
			jedis.lpush(USER_QUEUE, uids.toArray(new String[0]));
		}
	}

	public void setSongIds(List<String > sids){
		try (Jedis jedis = RedisUtil.getJedis()) {
			jedis.lpush(SONG_QUEUE, sids.toArray(new String[0]));
		}
	}

	public String getCrawlerSongid() {
		try (Jedis jedis = RedisUtil.getJedis()) {
			while (jedis.llen(SONG_QUEUE) <= 0) {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return jedis.rpop(SONG_QUEUE);
		}
	}

	public boolean needAdd() {

		try (Jedis jedis = RedisUtil.getJedis()) {
			return jedis.llen(USER_QUEUE) < 1000;
		}
	}




	/**
	 * 插入指定爬取的人
	 *
	 * @param uids
	 */
	public void specifyCrawlerUser(List<String> uids) {

		try (Jedis jedis = RedisUtil.getJedis()) {
			jedis.rpush(USER_QUEUE, uids.toArray(new String[0]));
		}
	}


	/**
	 * 初启动时，待爬取的种子用户
	 *
	 * @return
	 */
	public List<String> getCrawlerUserSeeds() {
		try (Jedis jedis = RedisUtil.getJedis()) {
			// seed 部分
			Long userCount = jedis.scard(USER_KEY);
			if (userCount == 0) {
				m163userSeeds = Lists.newArrayList(crawlerUserSeed.split(","));
			} else {
				m163userSeeds = jedis.srandmember(USER_KEY, 20);
			}
			LOGGER.info("crawler user seed {}", m163userSeeds);
			return m163userSeeds;
		}
	}

	@Value("${crawler.user.seed}")
	public void setCrawlerUserSeed(String crawlerUserSeed) {
		CrawlerBizConfig.crawlerUserSeed = crawlerUserSeed;
	}



	public boolean isCrawlSongSwitch() {
		return crawlSongSwitch;
	}

	public void setCrawlSongSwitch(boolean crawlSongSwitch) {
		this.crawlSongSwitch = crawlSongSwitch;
	}
}
