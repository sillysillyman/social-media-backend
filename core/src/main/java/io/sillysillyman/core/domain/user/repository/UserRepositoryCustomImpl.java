package io.sillysillyman.core.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.sillysillyman.core.domain.user.QUser;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final QUser qUser = QUser.user;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long deleteByDeletedAtNotNullAndBefore(Instant instant) {
        return jpaQueryFactory.delete(qUser)
            .where(qUser.deletedAt.isNotNull().and(qUser.deletedAt.loe(instant)))
            .execute();
    }
}
