package io.sillysillyman.api.controller.comment;


import io.sillysillyman.api.common.dto.PagedListResponse;
import io.sillysillyman.api.common.dto.SingleItemResponse;
import io.sillysillyman.api.controller.comment.dto.CommentResponse;
import io.sillysillyman.api.controller.comment.dto.CreateCommentRequest;
import io.sillysillyman.api.controller.comment.dto.UpdateCommentRequest;
import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.domain.comment.service.CommentService;
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
@RequestMapping("/api/v1/posts/{postId}/comments")
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<SingleItemResponse<CommentResponse>> createComment(
        @PathVariable Long postId,
        @Valid @RequestBody CreateCommentRequest createCommentRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            SingleItemResponse.from(
                CommentResponse.from(commentService.createComment(postId, createCommentRequest,
                    User.from(userDetails.userEntity()))
                )
            )
        );
    }

    @GetMapping
    public ResponseEntity<PagedListResponse<CommentResponse>> getComments(
        @PathVariable Long postId,
        @PageableDefault(sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
            PagedListResponse.from(
                commentService.getComments(postId, pageable).map(CommentResponse::from)
            )
        );
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @Valid @RequestBody UpdateCommentRequest updateCommentRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.updateComment(
            postId,
            commentId,
            updateCommentRequest,
            User.from(userDetails.userEntity())
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(postId, commentId, User.from(userDetails.userEntity()));
        return ResponseEntity.noContent().build();
    }
}
