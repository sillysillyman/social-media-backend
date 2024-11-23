package io.sillysillyman.core.domain.comment.service;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.ForbiddenAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.comment.command.CreateCommentCommand;
import io.sillysillyman.core.domain.comment.command.UpdateCommentCommand;
import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotBelongToPostException;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotFoundException;
import io.sillysillyman.core.domain.comment.repository.CommentRepository;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.service.PostService;
import io.sillysillyman.core.domain.user.User;
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
        return Comment.from(
            commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND)
            )
        );
    }

    @Transactional
    public Comment createComment(
        Long postId,
        CreateCommentCommand createCommentCommand,
        User user
    ) {
        Post post = postService.getById(postId);
        Comment comment = Comment.builder()
            .content(createCommentCommand.getContent())
            .post(post)
            .user(user)
            .build();

        CommentEntity commentEntity = commentRepository.save(CommentEntity.from(comment));

        return Comment.from(commentEntity);
    }

    @Transactional(readOnly = true)
    public Page<Comment> getComments(Long postId, Pageable pageable) {
        postService.getById(postId);
        return commentRepository.findByPostId(postId, pageable).map(Comment::from);
    }

    @Transactional
    public void updateComment(
        Long postId,
        Long commentId,
        UpdateCommentCommand updateCommentCommand,
        User user
    ) {
        Comment comment = getById(commentId);

        validateCommentOwnership(user.getId(), comment.getUser().getId());
        validateCommentPostId(postId, comment.getPost().getId());

        comment.update(updateCommentCommand);

        commentRepository.save(CommentEntity.from(comment));
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, User user) {
        Comment comment = getById(commentId);

        validateCommentOwnership(user.getId(), comment.getUser().getId());
        validateCommentPostId(postId, comment.getPost().getId());

        commentRepository.delete(CommentEntity.from(comment));
    }

    private void validateCommentOwnership(Long userId, Long authorId) {
        if (!Objects.equals(userId, authorId)) {
            throw new ForbiddenAccessException(AuthErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void validateCommentPostId(Long requestPostId, Long commentPostId) {
        if (!Objects.equals(requestPostId, commentPostId)) {
            throw new CommentNotBelongToPostException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }
    }
}
