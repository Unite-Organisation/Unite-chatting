package com.app.prod.config;

import com.app.prod.mocking.ApiTestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class IntegrationTest {

    @Autowired
    public ApiTestClient api;
    @Autowired
    protected ObjectMapper objectMapper;

    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("test-db")
                    .withUsername("test")
                    .withPassword("test")
                    .withExposedPorts(5433);

    static {
        postgres.setPortBindings(
                java.util.List.of("5434:5432")
        );
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    public AppUserRecord loggedUser() {
        return api.getLoggedUser();
    }

    public <T> T readResponse(ResultActions resultActions, TypeReference<T> typeReference) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, typeReference);
    }
}

