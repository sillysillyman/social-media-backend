package io.sillysillyman.socialmediabackend.domain.comment.service;

import io.sillysillyman.socialmediabackend.domain.comment.Comment;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CommentResponse;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CreateCommentRequest;
import io.sillysillyman.socialmediabackend.domain.comment.repository.CommentRepository;
import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.post.service.PostService;
import io.sillysillyman.socialmediabackend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    @Transactional
    public CommentResponse createComment(
        Long postId,
        CreateCommentRequest createCommentRequest,
        User user
    ) {
        Post post = postService.getById(postId);
        Comment comment = Comment.builder()
            .content(createCommentRequest.getContent())
            .post(post)
            .user(user)
            .build();
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }
}
