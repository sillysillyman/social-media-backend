package io.sillysillyman.core.domain.post.repository;

import io.sillysillyman.core.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUserId(Long userId, Pageable pageable);
}
