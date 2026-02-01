package com.app.prod.user.service;

import com.app.prod.exceptions.exceptions.EntityNotPresentException;
import com.app.prod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.AppUser;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public AppUserRecord findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with username: %s does not exist.", username))
        );
    }

    public AppUserRecord findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotPresentException(
                        String.format("User with id: %s does not exist.", userId),
                        AppUser.class.getSimpleName()
                )
        );
    }
}
