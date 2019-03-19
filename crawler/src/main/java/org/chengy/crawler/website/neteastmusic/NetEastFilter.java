package org.chengy.crawler.website.neteastmusic;


import org.chengy.crawler.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.BitSet;


/**
 * 该类 提供爬虫系统中的判重机制
 * 爬虫数据太小  用bloomfilter有误判率  可以使用bitmap
 * 每次启动从mongo移到内存太慢
 * 从redis中取
 */
@Component
public class NetEastFilter {


    private BitSet bitSet = new BitSet();

    private static final String USER_KEY = "user_id";
    private static final String SONG_KEY = "song_id";


    private ThreadLocal<Jedis> jedisThreadLocal = ThreadLocal.withInitial(RedisUtil::getJedis);


    @Value("${profile}")
    String env;

    @PostConstruct
    public void init() {

    }


    public boolean containsUid(String uid) {

        try (Jedis jedis = RedisUtil.getJedis()) {
            return jedis.sismember(USER_KEY, uid);
        }
//        Jedis jedis = jedisThreadLocal.get();
//        return jedis.sismember(USER_KEY, uid);
    }

    public boolean putUid(String uid) {
        //   Jedis jedis = jedisThreadLocal.get();

        try (Jedis jedis = RedisUtil.getJedis()) {
            jedis.sadd(USER_KEY, uid);
        }
        return true;
    }

    public boolean containsSongId(String songId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            return jedis.sismember(SONG_KEY, songId);

        }
    }

    public boolean putSongId(String songId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            jedis.sadd(SONG_KEY, songId);

        }
        return true;
    }
}
