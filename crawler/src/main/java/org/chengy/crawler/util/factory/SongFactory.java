package org.chengy.crawler.util.factory;


import org.chengy.crawler.model.Music163Song;
import org.chengy.crawler.website.neteastmusic.NetEastApiCons;

import java.util.List;

/**
 * Created by nali on 2017/9/27.
 */
public class SongFactory {




	public static Music163Song buildMusic163Song(String communityId, String lyric, String cover, List<String> arts, String albumTitle, String albumId, String title, String composer, String lyricist) {

		Music163Song song  = new Music163Song();
		song.setAlbumId(albumId);
		song.setAlbumTitle(albumTitle);
		song.setArts(arts);
		song.setCover(cover);
		song.setCommunity(NetEastApiCons.communityName);
		song.setId(communityId);
		song.setLyric(lyric);
		song.setLyricist(lyricist);
		song.setComposer(composer);
		song.setTitle(title);
		return song;
	}
}
