package com.app.prod.builders;

import com.app.prod.activity.enums.ActivityStatus;
import com.app.prod.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.ActivityRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityPersistanceFactory {

    private final Clock clock;
    private final ActivityRepository activityRepository;

    public Builder getNewActivity() {
        return new Builder();
    }

    public class Builder {

        private final ActivityRecord instance;

        public Builder() {
            instance = new ActivityRecord();
        }

        public Builder id(UUID id) {
            instance.setId(id);
            return this;
        }

        public Builder userId(UUID userId) {
            instance.setUserId(userId);
            return this;
        }

        public Builder lastSeen(LocalDateTime lastSeen) {
            instance.setLastSeen(lastSeen);
            return this;
        }

        public Builder status(String status) {
            instance.setStatus(status);
            return this;
        }

        public Builder withRandomValues() {
            instance.setId(UUID.randomUUID());
            instance.setUserId(UUID.randomUUID());
            instance.setLastSeen(LocalDateTime.now(clock));
            instance.setStatus(ActivityStatus.ONLINE.name());
            return this;
        }

        public ActivityRecord build() {
            return instance;
        }

        public ActivityRecord buildAndSave() {
            ActivityRecord record = build();
            activityRepository.insertOne(record);
            return record;
        }
    }

    public void batchBuildAndSave(List<ActivityRecord> activities) {
        activityRepository.insertMany(activities);
    }
}