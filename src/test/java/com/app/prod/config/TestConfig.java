package com.app.prod.config;

import com.app.prod.mocking.TestStorage;
import com.app.prod.storage.AbstractStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TestConfig {

    @Bean
    @Profile("test")
    public AbstractStorage testStorage() {
        return new TestStorage();
    }

}
