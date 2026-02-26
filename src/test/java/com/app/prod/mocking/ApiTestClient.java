package com.app.prod.mocking;

import com.app.prod.builders.UserPersistanceFactory;
import com.app.prod.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.annotation.PostConstruct;

import static com.app.prod.config.Constants.BCRYPT_PASSWORD_ENCODER_STRENGTH;


@Slf4j
@Component
public class ApiTestClient {

    private final String PASSWORD = "testpassword";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPersistanceFactory userPersistanceFactory;

    private AppUserRecord loggedUser;
    private String token;

    public ResultActions performAuthenticated(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        if (loggedUser == null) {
            createRandomUserAndLogIn();
        }

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        } else {
            log.error("User not authenticated, no token provided");
        }

        return mockMvc.perform(requestBuilder);
    }

    @PostConstruct
    private void createRandomUserAndLogIn() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_PASSWORD_ENCODER_STRENGTH);

        loggedUser = userPersistanceFactory.getNewUser()
                .withRandomValues()
                .password(passwordEncoder.encode(PASSWORD))
                .buildAndSave();

        setTokenForUser(loggedUser, PASSWORD);
    }

    private void setTokenForUser(AppUserRecord user, String password) {
        token = userService.generateToken(user.getUsername(), password);
    }

    public ApiTestClient login(AppUserRecord user) {
        loggedUser = user;
        String rawPassword = user.getPassword();
        encodeUsersPassword(user);
        setTokenForUser(user, rawPassword);
        return this;
    }

    private void encodeUsersPassword(AppUserRecord user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_PASSWORD_ENCODER_STRENGTH);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userService.updatePassword(user.getId(), encodedPassword);
    }

    public String getToken() {
        return token;
    }

    public AppUserRecord getLoggedUser() {
        return loggedUser;
    }
}
