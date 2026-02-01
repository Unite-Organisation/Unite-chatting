package com.app.prod.user.repository;

import com.app.prod.utils.BaseJooqRepository;
import org.jooq.DSLContext;
import org.jooq.sources.tables.AppUser;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.stereotype.Repository;

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

}
