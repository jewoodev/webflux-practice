package com.heri2go.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.heri2go.chat.domain.chat.ChatRepository;

@ActiveProfiles("test")
@DataMongoTest
public abstract class MongoTestSupport {
    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected ReactiveMongoTemplate reactiveMongoTemplate;
}
