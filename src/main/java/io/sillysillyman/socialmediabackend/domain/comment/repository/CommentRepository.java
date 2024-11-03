package io.sillysillyman.socialmediabackend.domain.comment.repository;

import io.sillysillyman.socialmediabackend.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
