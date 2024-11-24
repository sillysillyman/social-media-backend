package io.sillysillyman.api.controller.reply;

import io.sillysillyman.api.common.dto.PagedListResponse;
import io.sillysillyman.api.common.dto.SingleItemResponse;
import io.sillysillyman.api.controller.reply.dto.ReplyResponse;
import io.sillysillyman.api.controller.reply.dto.UpsertReplyRequest;
import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.domain.reply.service.ReplyService;
import io.sillysillyman.core.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/comments/{commentId}/replies")
@RestController
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping
    public ResponseEntity<SingleItemResponse<ReplyResponse>> createReply(
        @PathVariable Long commentId,
        @Valid @RequestBody UpsertReplyRequest upsertReplyRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            SingleItemResponse.from(
                ReplyResponse.from(
                    replyService.createReply(
                        commentId,
                        upsertReplyRequest,
                        User.from(userDetails.userEntity())
                    )
                )
            )
        );
    }

    @GetMapping
    public ResponseEntity<PagedListResponse<ReplyResponse>> getReplies(
        @PathVariable Long commentId,
        @PageableDefault(sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
            PagedListResponse.from(
                replyService.getReplies(commentId, pageable).map(ReplyResponse::from)
            )
        );
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<Void> updateReply(
        @PathVariable Long commentId,
        @PathVariable Long replyId,
        @Valid @RequestBody UpsertReplyRequest upsertReplyRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        replyService.updateReply(
            commentId,
            replyId,
            upsertReplyRequest,
            User.from(userDetails.userEntity())
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(
        @PathVariable Long commentId,
        @PathVariable Long replyId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        replyService.deleteReply(commentId, replyId, User.from(userDetails.userEntity()));
        return ResponseEntity.noContent().build();
    }
}
