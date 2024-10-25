package io.sillysillyman.socialmediabackend.domain.user;


import io.sillysillyman.socialmediabackend.common.dto.SingleItemBody;
import io.sillysillyman.socialmediabackend.domain.user.dto.ChangePasswordDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.SignupDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    ResponseEntity<SingleItemBody<UserDto>> signup(@Valid @RequestBody SignupDto signupDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new SingleItemBody<>(userService.signup(signupDto)));
    }

    @PutMapping("/{userId}/password")
    ResponseEntity<Void> changePassword(
        @PathVariable Long userId,
        @Valid @RequestBody ChangePasswordDto changePasswordDto
    ) {
        userService.changePassword(userId, changePasswordDto);
        return ResponseEntity.ok().build();
    }
}
