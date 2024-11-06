package io.sillysillyman.core.domain.comment.repository;

import io.sillysillyman.core.domain.comment.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    Page<CommentEntity> findByPostId(Long postId, Pageable pageable);
}
