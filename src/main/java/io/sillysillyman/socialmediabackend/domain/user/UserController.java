package io.sillysillyman.socialmediabackend.domain.user;


import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.common.dto.SingleItemBody;
import io.sillysillyman.socialmediabackend.domain.user.dto.ChangePasswordDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.SignupDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserDto;
import io.sillysillyman.socialmediabackend.domain.user.service.UserService;
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
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    ResponseEntity<SingleItemBody<UserDto>> signup(@Valid @RequestBody SignupDto signupDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SingleItemBody.from(userService.signup(signupDto)));
    }

    @GetMapping("/{userId}")
    ResponseEntity<SingleItemBody<UserDto>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(SingleItemBody.from(userService.getUser(userId)));
    }

    @PutMapping("/me/password")
    ResponseEntity<Void> changePassword(
        @Valid @RequestBody ChangePasswordDto changePasswordDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.changePassword(changePasswordDto, userDetails.user());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.user());
        return ResponseEntity.noContent().build();
    }
}
