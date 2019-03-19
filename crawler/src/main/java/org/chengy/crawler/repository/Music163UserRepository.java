package org.chengy.crawler.repository;

import org.chengy.crawler.model.Music163User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Music163UserRepository extends MongoRepository<Music163User, String> {

    long countAllBySongRecordIsTrue();
}
