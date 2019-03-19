package org.chengy.crawler.model;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class Music163User extends BaseModel {
	private String username;
	private String avatar;
	private String signature;
	private Date age;
	/**
	 * 1:male 2:female
	 * 0 present unknow
	 */
	private int gender;
	private String area;
	/**
	 * 听歌数量
	 */
	private int recordSongNum;


	/**
	 * 记录过歌曲
	 */
	private Boolean songRecord;

	/**
	 * 分析过歌曲
	 */
	private Boolean songAnalyzed;

	private List<String> loveSongId;

	private List<Pair<String, Integer>> songScore;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}


	@Override
	public String toString() {
		return username + "@" + this.getCommunity() + " in " + area + "：" + signature;
	}

	public List<String> getLoveSongId() {
		return loveSongId;
	}

	public void setLoveSongId(List<String> loveSongId) {
		this.loveSongId = loveSongId;
	}

	public Date getAge() {
		return age;
	}

	public void setAge(Date age) {
		this.age = age;
	}


	public Boolean getSongRecord() {
		return songRecord;
	}

	public void setSongRecord(Boolean songRecord) {
		this.songRecord = songRecord;
	}

	public List<Pair<String, Integer>> getSongScore() {
		return songScore;
	}

	public void setSongScore(List<Pair<String, Integer>> songScore) {
		this.songScore = songScore;
	}

	public int getRecordSongNum() {
		return recordSongNum;
	}

	public void setRecordSongNum(int recordSongNum) {
		this.recordSongNum = recordSongNum;
	}

	public Boolean getSongAnalyzed() {
		return songAnalyzed;
	}

	public void setSongAnalyzed(Boolean songAnalyzed) {
		this.songAnalyzed = songAnalyzed;
	}
}
