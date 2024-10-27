package io.sillysillyman.socialmediabackend.domain.user;

import io.sillysillyman.socialmediabackend.domain.user.service.UserSchedulerService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "UserCleanupScheduler")
@Component
@RequiredArgsConstructor
public class UserScheduler {

    private final UserSchedulerService userSchedulerService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldSoftDeletedUsers() {
        Instant sixMonthsAgo = Instant.now().minus(6, ChronoUnit.MONTHS);
        long deletedCount = userSchedulerService.deleteOldSoftDeletedUsers(sixMonthsAgo);
        log.info("Completed cleanup of old deleted users. Total deleted: {}", deletedCount);
    }
}
