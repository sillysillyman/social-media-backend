package io.sillysillyman.socialmediabackend.domain.user.repository;

import java.time.Instant;

public interface UserRepositoryCustom {

    long deleteByDeletedAtNotNullAndBefore(Instant instant);
}