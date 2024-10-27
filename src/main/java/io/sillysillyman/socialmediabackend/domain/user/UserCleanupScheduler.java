package io.sillysillyman.socialmediabackend.domain.user;

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
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldDeletedUsers() {
        Instant sixMonthsAgo = Instant.now().minus(6, ChronoUnit.MONTHS);
        long deletedCount = userRepository.deleteOlderThan(sixMonthsAgo);
        log.info("Completed cleanup of old deleted users. Total deleted: {}", deletedCount);
    }
}
