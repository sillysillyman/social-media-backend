package io.sillysillyman.socialmediabackend.domain.user.service;

import io.sillysillyman.socialmediabackend.domain.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSchedulerService {

    private final UserRepository userRepository;

    @Transactional
    public long deleteOldSoftDeletedUsers(Instant instant) {
        return userRepository.deleteByDeletedAtNotNullAndBefore(instant);
    }
}
