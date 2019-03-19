package org.chengy.crawler.website.neteastmusic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.model.Music163Song;
import org.chengy.crawler.model.Music163User;
import org.chengy.crawler.util.factory.SongFactory;
import org.chengy.crawler.util.factory.UserFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class NetEastParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetEastParser.class);

	@Autowired
	ObjectMapper objectMapper;

	public Music163User parseUser(String html, String id) {
		try {
			Document document = Jsoup.parse(html);
			// 听歌总数
			int songNums = 0;
			try {
				String songNumsInfo =
						document.select("#rHeader > h4").get(0).html();
				songNums = Integer.parseInt(songNumsInfo.substring(4, songNumsInfo.length() - 1));
			} catch (Exception e) {
				LOGGER.warn("获取用户 " + id + " 听歌数量失败");
			}
			//性别
			boolean ismale = document.select("#j-name-wrap > i").hasClass("u-icn-01");
			boolean isfemale = document.select("#j-name-wrap > i").hasClass("u-icn-02");
			int gender = 0;
			if (ismale) {
				gender = 1;
			} else if (isfemale) {
				gender = 2;
			}
			String name = document.select("#j-name-wrap > span.tit.f-ff2.s-fc0.f-thide").get(0).html();
			//个性签名
			Elements signatureinfo = document.select("#head-box > dd > div.inf.s-fc3.f-brk");
			String signature = "";
			if (signatureinfo.size() > 0) {
				signature = signatureinfo.get(0).html().split("：")[1];
			}
			//年龄
			Elements ageinfo = document.select("#age");
			Date age = null;
			if (ageinfo.size() > 0) {
				age = new Date(Long.parseLong(ageinfo.get(0).attr("data-age")));
			}

			String area = null;
			//地区的代码逻辑
			try {
				Elements elements = document.select("#head-box > dd > div:nth-child(4) > span:nth-child(1)");
				if (elements.size() > 0) {
					try {
						area = elements.get(0).html().split("：")[1];
					} catch (Exception e) {
						elements = document.select("#head-box > dd > div:nth-child(3) > span:nth-child(1)");
						area = elements.get(0).html().split("：")[1];
					}
				} else {
					elements = document.select("#head-box > dd > div.inf.s-fc3 > span");
					if (elements.size() > 0) {
						area = elements.get(0).html().split("：")[1];
					}
				}
			} catch (Exception e) {
				LOGGER.warn("get area of user {} failed!", id);
			}

			String avatar = document.select("#ava > img").attr("src");
			Music163User user = UserFactory.buildMusic163User(age, area, name, avatar, id, signature, gender, songNums);
			return user;
		} catch (Exception e) {
			LOGGER.error("parse user {} error!", id, e);
			throw e;
		}

	}

	public List<String> followedUsers(String jsonStr) {
		List<String> followedUsers = new ArrayList<>();
		try {
			JsonNode root = objectMapper.readTree(jsonStr);
			List<JsonNode> jsonNodeList =
					root.findValue("followeds").findValues("userId");

			List<String> ids =
					jsonNodeList.stream().map(JsonNode::asText).collect(Collectors.toList());
			followedUsers.addAll(ids);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return followedUsers;
	}

	public List<String> fansUsers(String jsonStr) {
		List<String> fansUsers = new ArrayList<>();
		try {
			JsonNode root = objectMapper.readTree(jsonStr);
			List<String> ids =
					root.findValue("follow").findValues("userId")
							.stream().map(ob -> ob.asText()).collect(Collectors.toList());
			fansUsers.addAll(ids);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fansUsers;
	}

	public List<Pair<String, Integer>> userLoveSongScores(String uid, String jsonStr) {
		List<Pair<String, Integer>> pairList = new ArrayList<>();

		try {
			JsonNode root = objectMapper.readTree(jsonStr);
			root.findValue("allData").iterator().forEachRemaining(ob -> {
				String songId = ob.get("song").get("id").asText();
				int score = ob.get("score").asInt();
				pairList.add(new ImmutablePair<>(songId, score));
			});
		} catch (Exception e) {
			LOGGER.error(uid + "get user love song failed" + jsonStr, e);
		}
		return pairList;
	}

	public Music163Song parseSong(String html, String songId, String lyric) {

		Document document = Jsoup.parse(html);

		Elements titleEle = document.select("body > div.g-bd4.f-cb > div.g-mn4 > div > div > div.m-lycifo > div.f-cb > div.cnt > div.hd > div > em");
		String title = titleEle.get(0).html();
		String cover = document.select("div.u-cover.u-cover-6.f-fl > img").get(0).attr("src");

		Elements artsELes = document.select("body > div.g-bd4.f-cb > div.g-mn4 > div > div > div.m-lycifo > div.f-cb > div.cnt > p:nth-child(2)");
		String art = artsELes.text().split("：")[1].trim();

		Elements albumEle = document.select("body > div.g-bd4.f-cb > div.g-mn4 > div > div > div.m-lycifo > div.f-cb > div.cnt > p:nth-child(3) > a");
		String albumTitle = albumEle.get(0).html();
		String albumId = albumEle.get(0).attr("href").split("id=")[1];

		List<String> arts = new ArrayList<>();
		Arrays.asList(art.split("/")).forEach(ob -> arts.add(ob.trim()));

		try {
			JsonNode root = objectMapper.readTree(lyric);
			try {
				lyric = root.findValue("lrc").findValue("lyric").asText();
			} catch (Exception e) {
				lyric = root.findValue("tlyric").findValue("lyric").asText();
			}
			String composer = "";
			String pattern = "作曲 : .*?\n";
			Pattern r = Pattern.compile(pattern);
			Matcher matcher = r.matcher(lyric);
			while (matcher.find()) {
				composer = matcher.group().split(":")[1].trim();
			}
			String lyricist = "";
			pattern = "作词 : .*?\n";
			r = Pattern.compile(pattern);
			matcher = r.matcher(lyric);
			while (matcher.find()) {
				lyricist = matcher.group().split(":")[1].trim();
			}
			Music163Song song = SongFactory.buildMusic163Song(songId, lyric, cover, arts, albumTitle, albumId, title, composer, lyricist);
			System.out.println(song);

			return song;
		} catch (Exception e) {
			Music163Song song = SongFactory.buildMusic163Song(songId, "", cover, arts, albumTitle, albumId, title, "", "");
			System.out.println(song);
			return song;
		}

	}
}
