package org.chengy.crawler.website.neteastmusic;

import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.model.Music163User;

import java.util.List;

public class CrawlerUserInfo {

    private Music163User user;
    private List<String> relativeIds;
    private List<Pair<String, Integer>> loveSongs;


    public Music163User getUser() {
        return user;
    }

    public void setUser(Music163User user) {
        this.user = user;
    }

    public List<String> getRelativeIds() {
        return relativeIds;
    }

    public void setRelativeIds(List<String> relativeIds) {
        this.relativeIds = relativeIds;
    }


    public CrawlerUserInfo(Music163User user, List<String> relativeIds) {
        this.user = user;
        this.relativeIds = relativeIds;
    }


    public List<Pair<String, Integer>> getLoveSongs() {
        return loveSongs;
    }

    public void setLoveSongs(List<Pair<String, Integer>> loveSongs) {
        this.loveSongs = loveSongs;
    }
}
