package io.sillysillyman.socialmediabackend.domain.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final QUser qUser = QUser.user;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long deleteOlderThan(Instant date) {
        return jpaQueryFactory.delete(qUser)
            .where(qUser.deletedAt.isNotNull().and(qUser.deletedAt.loe(date)))
            .execute();
    }
}
