package org.chengy.crawler.util.factory;

import org.chengy.crawler.model.Music163User;
import org.chengy.crawler.website.neteastmusic.NetEastApiCons;

import java.util.Date;

/**
 * Created by nali on 2017/9/15.
 */
public class UserFactory {



	public static Music163User buildMusic163User(Date age, String area, String nickname, String avatar, String uid, String signature, int gender, int songNums){

		Music163User user = new Music163User();
		user.setAge(age);
		user.setArea(area);
		user.setUsername(nickname);
		user.setAvatar(avatar);
		user.setCommunity(NetEastApiCons.communityName);
		user.setId(uid);
		user.setSignature(signature);
		user.setGender(gender);
		user.setRecordSongNum(songNums);
		return user;
	}
}
