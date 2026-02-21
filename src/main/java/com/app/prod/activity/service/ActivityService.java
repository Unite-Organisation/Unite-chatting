package com.app.prod.activity.service;

import com.app.prod.activity.enums.ActivityStatus;
import com.app.prod.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.records.ActivityRecord;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final Clock clock;

    public void createStatusForEveryUser(ActivityStatus status) {
        List<UUID> userIds = activityRepository.findAllUsersWithoutActivityStatus();
        List<ActivityRecord> batch = userIds.stream()
                .map(userId -> new ActivityRecord(
                        UUID.randomUUID(),
                        userId,
                        LocalDateTime.now(clock),
                        status.name()
                ))
                .toList();

        log.info("Creating {} activity records for status: {}", batch.size(), status);
        activityRepository.insertMany(batch);
    }

    public void updateActivityStatus(UUID userId, ActivityStatus status) {
        ActivityRecord userActivityRecord = activityRepository.findByUserId(userId);

        // only scenario when we dont want any update is when status was offline and update is also offline
        if (!(ActivityStatus.OFFLINE.equals(status) && ActivityStatus.OFFLINE.name().equals(userActivityRecord.getStatus()))) {
            activityRepository.updateStatus(userId, status);
        }

    }

}
