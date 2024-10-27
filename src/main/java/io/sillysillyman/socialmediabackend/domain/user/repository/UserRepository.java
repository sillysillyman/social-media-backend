package io.sillysillyman.socialmediabackend.domain.user.repository;

import io.sillysillyman.socialmediabackend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Boolean existsByUsername(String username);
}
