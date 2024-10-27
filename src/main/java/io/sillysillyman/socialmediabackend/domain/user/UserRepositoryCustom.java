package io.sillysillyman.socialmediabackend.domain.user;

import java.time.Instant;

public interface UserRepositoryCustom {

    long deleteOlderThan(Instant instant);
}
