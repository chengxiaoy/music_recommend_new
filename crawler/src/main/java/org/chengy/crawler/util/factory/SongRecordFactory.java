package org.chengy.crawler.util.factory;


import org.chengy.crawler.model.Music163SongRecord;

import java.util.Collections;

/**
 * Created by nali on 2017/11/27.
 */
public class SongRecordFactory {




    public static Music163SongRecord buildMusic163SongRecord(String commuId, String commuName, int loveNum, long score, String uid) {
        Music163SongRecord music163SongRecord = new Music163SongRecord();
        music163SongRecord.setCommunity(commuName);
        music163SongRecord.setId(commuId);
        music163SongRecord.setLoveNum(loveNum);
        music163SongRecord.setScore(score);
        music163SongRecord.setLoverIds(Collections.singletonList(uid));
        return music163SongRecord;
    }


}
