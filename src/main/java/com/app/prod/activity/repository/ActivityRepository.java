package com.app.prod.activity.repository;

import com.app.prod.activity.enums.ActivityStatus;
import com.app.prod.utils.BaseJooqRepository;
import org.jooq.DSLContext;
import org.jooq.sources.tables.Activity;
import org.jooq.sources.tables.records.ActivityRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.jooq.sources.Tables.ACTIVITY;
import static org.jooq.sources.Tables.APP_USER;

@Repository
public class ActivityRepository extends BaseJooqRepository<Activity, ActivityRecord, UUID> {
    protected ActivityRepository(DSLContext dsl) {
        super(dsl, ACTIVITY, ACTIVITY.ID);
    }

    public List<UUID> findAllUsersWithoutActivityStatus() {
        return dslContext.select(APP_USER.ID)
                .from(APP_USER)
                .leftOuterJoin(ACTIVITY).on(APP_USER.ID.eq(ACTIVITY.USER_ID))
                .where(ACTIVITY.USER_ID.isNull())
                .fetch(APP_USER.ID);

    }

    public void updateStatus(UUID userId, ActivityStatus status, LocalDateTime now) {
        dslContext.update(ACTIVITY)
                .set(ACTIVITY.STATUS, status.name())
                .set(ACTIVITY.LAST_SEEN, now)
                .where(ACTIVITY.USER_ID.eq(userId))
                .execute();
    }

    public ActivityRecord findByUserId(UUID userId) {
        return dslContext.selectFrom(ACTIVITY)
                .where(ACTIVITY.USER_ID.eq(userId))
                .fetchOne();
    }
}
