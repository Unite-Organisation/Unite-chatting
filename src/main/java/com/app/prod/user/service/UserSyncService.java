package com.app.prod.user.service;

import com.app.prod.internal.dtos.UserDto;
import com.app.prod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;
    private final Clock clock;

    public void syncUser(UserDto userDto){
        if (userRepository.exists(userDto.id())) {
            userRepository.updateUser(userDto);
        } else {
            userRepository.insertOne(new AppUserRecord(
                    userDto.id(),
                    userDto.firstName(),
                    userDto.lastName(),
                    userDto.email(),
                    userDto.username(),
                    userDto.password(),
                    userDto.userRole(),
                    userDto.status().name(),
                    LocalDateTime.now(clock)
            ));
        }
    }

}
