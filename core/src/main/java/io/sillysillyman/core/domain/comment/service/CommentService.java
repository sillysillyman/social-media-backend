package io.sillysillyman.core.domain.comment.service;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotBelongToPostException;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotFoundException;
import io.sillysillyman.core.domain.comment.repository.CommentRepository;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.service.PostService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
            new CommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND)
        );
    }

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

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable).map(CommentResponse::from);
    }

    @Transactional
    public void updateComment(
        Long postId,
        Long commentId,
        UpdateCommentRequest updateCommentRequest,
        User user
    ) {
        Comment comment = getById(commentId);

        validateCommentOwnership(user.getId(), comment.getUser().getId());
        validateCommentPostId(postId, comment.getPost().getId());

        comment.update(updateCommentRequest);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, User user) {
        Comment comment = getById(commentId);

        validateCommentOwnership(user.getId(), comment.getUser().getId());
        validateCommentPostId(postId, comment.getPost().getId());

        commentRepository.delete(comment);
    }

    private void validateCommentOwnership(Long userId, Long authorId) {
        if (!Objects.equals(authorId, userId)) {
            throw new UnauthorizedAccessException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateCommentPostId(Long requestPostId, Long commentPostId) {
        if (!Objects.equals(requestPostId, commentPostId)) {
            throw new CommentNotBelongToPostException(
                io.sillysillyman.socialmediabackend.domain.comment.exception.CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }
    }
}
