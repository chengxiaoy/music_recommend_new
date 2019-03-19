package org.chengy.crawler.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Music163Song extends BaseModel {
	private String title;
	private String albumTitle;
	private String cover;

	private String albumId;
	private List<String> arts;
	private List<String> tags;
	private String category;
	private String language;

	private String composer;
	private String lyricist;
	private String lyric;

	public String toString() {
		return "arts: " + arts + " title: " + title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public List<String> getArts() {
		return arts;
	}

	public void setArts(List<String> arts) {
		this.arts = arts;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getLyricist() {
		return lyricist;
	}

	public void setLyricist(String lyricist) {
		this.lyricist = lyricist;
	}

	public String getLyric() {
		return lyric;
	}

	public void setLyric(String lyric) {
		this.lyric = lyric;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}
}
