package io.sillysillyman.core.domain.user.repository;

import io.sillysillyman.core.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
