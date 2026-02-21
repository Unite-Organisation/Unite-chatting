package com.app.prod.user.service;

import com.app.prod.activity.repository.ActivityRepository;
import com.app.prod.internal.dtos.UserDto;
import com.app.prod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.ActivityRecord;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final Clock clock;

    public void syncUser(UserDto userDto){
        createUserRecord(userDto);
        createActivityRecord(userDto);
    }

    private void createUserRecord(UserDto userDto) {
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

    private void createActivityRecord(UserDto userDto) {
        if (!activityRepository.exists(userDto.id())) {
            activityRepository.insertOne(new ActivityRecord(
                    UUID.randomUUID(),
                    userDto.id(),
                    LocalDateTime.now(clock),
                    "OFFLINE"
            ));
        }
    }

}
