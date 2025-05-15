package com.heri2go.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig {

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDatabaseFactory);

        // _class 필드 저장 비활성화
        ((MappingMongoConverter) template.getConverter())
                .setTypeMapper(new DefaultMongoTypeMapper(null));

        return template;
    }
}
