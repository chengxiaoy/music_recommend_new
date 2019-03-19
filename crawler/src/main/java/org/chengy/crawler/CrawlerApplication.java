package org.chengy.crawler;

import org.chengy.crawler.website.neteastmusic.CrawlerBizConfig;
import org.chengy.crawler.website.neteastmusic.NetEastMusicFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrawlerApplication implements CommandLineRunner {
	@Autowired
	NetEastMusicFacade netEastMusicFacade;

	@Autowired
	CrawlerBizConfig crawlerBizConfig;

	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		crawlerBizConfig.setCrawlSongSwitch(true);
		netEastMusicFacade.crawlM163Songs();
	}
}
