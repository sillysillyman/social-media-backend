package io.sillysillyman.api.controller.user;


import io.sillysillyman.api.common.dto.PagedListResponse;
import io.sillysillyman.api.common.dto.SingleItemResponse;
import io.sillysillyman.api.controller.post.dto.PostResponse;
import io.sillysillyman.api.controller.user.dto.ChangePasswordRequest;
import io.sillysillyman.api.controller.user.dto.SignupRequest;
import io.sillysillyman.api.controller.user.dto.UserResponse;
import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.domain.post.service.PostService;
import io.sillysillyman.core.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final PostService postService;
    private final UserService userService;

    @PostMapping("/signup")
    ResponseEntity<SingleItemResponse<UserResponse>> signup(
        @Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SingleItemResponse.from(userService.signup(signupRequest)));
    }

    @GetMapping("/{userId}")
    ResponseEntity<SingleItemResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(SingleItemResponse.from(userService.getUser(userId)));
    }

    @GetMapping("/{userId}/posts")
    ResponseEntity<PagedListResponse<PostResponse>> getUserPosts(@PathVariable Long userId,
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            PagedListResponse.from(postService.getUserPosts(userId, pageable))
        );
    }

    @GetMapping("/me/posts")
    ResponseEntity<PagedListResponse<PostResponse>> getMyPosts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            PagedListResponse.from(postService.getMyPosts(userDetails.user(), pageable))
        );
    }

    @PutMapping("/me/password")
    ResponseEntity<Void> changePassword(
        @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.changePassword(changePasswordRequest, userDetails.user());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.user());
        return ResponseEntity.noContent().build();
    }
}
