package io.sillysillyman.core.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.sillysillyman.core.domain.user.QUserEntity;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final QUserEntity qUserEntity = QUserEntity.userEntity;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long deleteByDeletedAtNotNullAndBefore(Instant instant) {
        return jpaQueryFactory.delete(qUserEntity)
            .where(qUserEntity.deletedAt.isNotNull().and(qUserEntity.deletedAt.loe(instant)))
            .execute();
    }
}
