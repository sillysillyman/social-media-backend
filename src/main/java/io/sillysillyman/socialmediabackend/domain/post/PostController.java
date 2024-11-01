package io.sillysillyman.socialmediabackend.domain.post;

import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.common.dto.SingleItemResponse;
import io.sillysillyman.socialmediabackend.domain.post.dto.CreatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
import io.sillysillyman.socialmediabackend.domain.post.dto.UpdatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/posts")
@RestController
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<SingleItemResponse<PostResponse>> createPost(
        @Valid @RequestBody CreatePostRequest createPostRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleItemResponse.from(null));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<SingleItemResponse<PostResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(SingleItemResponse.from(null));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
        @PathVariable Long postId,
        @RequestBody UpdatePostRequest updatePostRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.noContent().build();
    }
}
