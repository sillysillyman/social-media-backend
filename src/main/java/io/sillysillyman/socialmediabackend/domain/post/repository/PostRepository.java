package io.sillysillyman.socialmediabackend.domain.post.repository;

import io.sillysillyman.socialmediabackend.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
