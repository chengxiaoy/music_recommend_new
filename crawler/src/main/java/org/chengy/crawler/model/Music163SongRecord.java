package org.chengy.crawler.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document
public class Music163SongRecord extends BaseModel {
	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Integer getLoveNum() {
		return loveNum;
	}

	public void setLoveNum(Integer loveNum) {
		this.loveNum = loveNum;
	}

	public List<String> getLoverIds() {
		return loverIds;
	}

	public void setLoverIds(List<String> loverIds) {
		this.loverIds = loverIds;
	}

	/**
	 * 喜爱的分值
	 */
	private Long score;
	/**
	 * 喜爱的人数（top100中包含此歌的人数）
	 */
	private Integer loveNum;
	/**
	 * 可以将此歌作为特征的人的id记录
	 */
	private List<String> loverIds;

}