package org.chengy.crawler.repository;

import org.chengy.crawler.model.Music163SongRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Music163SongRecordRepository extends MongoRepository<Music163SongRecord,String> {

}
