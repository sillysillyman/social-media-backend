package io.sillysillyman.core.domain.user.repository;

import io.sillysillyman.core.domain.user.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom {

    Boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
}
