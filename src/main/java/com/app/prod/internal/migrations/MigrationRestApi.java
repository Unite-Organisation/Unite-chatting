package com.app.prod.internal.migrations;

import com.app.prod.activity.enums.ActivityStatus;
import com.app.prod.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("internal/migration")
@RequiredArgsConstructor
@Tag(name = "Migrations")
public class MigrationRestApi {

    private final ActivityService activityService;

    @GetMapping("/activity-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void migrateActivityStatus(@RequestParam("status") ActivityStatus status) {
        activityService.createStatusForEveryUser(status);
    }
}
