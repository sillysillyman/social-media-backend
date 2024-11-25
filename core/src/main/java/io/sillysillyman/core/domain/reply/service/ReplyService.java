package io.sillysillyman.core.domain.reply.service;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.ForbiddenAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.service.CommentService;
import io.sillysillyman.core.domain.reply.Reply;
import io.sillysillyman.core.domain.reply.ReplyEntity;
import io.sillysillyman.core.domain.reply.command.UpsertReplyCommand;
import io.sillysillyman.core.domain.reply.exception.ReplyErrorCode;
import io.sillysillyman.core.domain.reply.exception.detail.ReplyNotBelongToCommentException;
import io.sillysillyman.core.domain.reply.exception.detail.ReplyNotFoundException;
import io.sillysillyman.core.domain.reply.repository.ReplyRepository;
import io.sillysillyman.core.domain.user.User;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentService commentService;

    @Transactional(readOnly = true)
    public Reply getById(Long replyId) {
        return Reply.from(
            replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException(ReplyErrorCode.REPLY_NOT_FOUND))
        );
    }

    @Transactional
    public Reply createReply(Long commentId, UpsertReplyCommand upsertReplyCommand, User user) {
        Comment comment = commentService.getById(commentId);
        Reply reply = Reply.builder()
            .content(upsertReplyCommand.content())
            .comment(comment)
            .user(user)
            .build();

        ReplyEntity replyEntity = replyRepository.save(ReplyEntity.from(reply));

        return Reply.from(replyEntity);
    }

    @Transactional(readOnly = true)
    public Page<Reply> getReplies(Long commentId, Pageable pageable) {
        commentService.getById(commentId);
        return replyRepository.findByCommentId(commentId, pageable).map(Reply::from);
    }

    @Transactional
    public void updateReply(
        Long commentId,
        Long replyId,
        UpsertReplyCommand upsertReplyCommand,
        User user
    ) {
        Reply reply = getById(replyId);

        validateReplyOwnership(user.getId(), reply.getUser().getId());
        validateReplyCommentId(commentId, reply.getComment().getId());

        reply.update(upsertReplyCommand);

        replyRepository.save(ReplyEntity.from(reply));
    }

    @Transactional
    public void deleteReply(
        Long commentId,
        Long replyId,
        User user
    ) {
        Reply reply = getById(replyId);

        validateReplyOwnership(user.getId(), reply.getUser().getId());
        validateReplyCommentId(commentId, reply.getComment().getId());

        replyRepository.delete(ReplyEntity.from(reply));
    }

    private void validateReplyOwnership(Long userId, Long authorId) {
        if (!Objects.equals(userId, authorId)) {
            throw new ForbiddenAccessException(AuthErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void validateReplyCommentId(Long requestCommentId, Long replyCommentId) {
        if (!Objects.equals(requestCommentId, replyCommentId)) {
            throw new ReplyNotBelongToCommentException(ReplyErrorCode.REPLY_NOT_BELONG_TO_COMMENT);
        }
    }
}
