package com.app.prod.user.repository;

import com.app.prod.internal.dtos.UserDto;
import com.app.prod.utils.BaseJooqRepository;
import org.jooq.DSLContext;
import org.jooq.sources.tables.AppUser;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.sources.Tables.*;

@Repository
public class UserRepository extends BaseJooqRepository<AppUser, AppUserRecord, UUID> {
    protected UserRepository(DSLContext dsl) {
        super(dsl, APP_USER, APP_USER.ID);
    }

    public Optional<AppUserRecord> findByUsername(String username){
        return dslContext.selectFrom(table)
                .where(table.USERNAME.eq(username))
                .fetchOptional();
    }

    public void updateUser(UserDto userDto) {
        dslContext.update(APP_USER)
                .set(APP_USER.USERNAME, userDto.username())
                .set(APP_USER.EMAIL, userDto.email())
                .set(APP_USER.FIRST_NAME, userDto.firstName())
                .set(APP_USER.LAST_NAME, userDto.lastName())
                .set(APP_USER.PASSWORD, userDto.password())
                .set(APP_USER.USER_ROLE, userDto.userRole())
                .set(APP_USER.STATUS, userDto.status().name())
                .where(APP_USER.ID.eq(userDto.id()))
                .execute();
    }

    public void updatePassword(UUID userId, String password){
        dslContext.update(APP_USER)
                .set(APP_USER.PASSWORD, password)
                .where(APP_USER.ID.eq(userId))
                .execute();
    }

}
