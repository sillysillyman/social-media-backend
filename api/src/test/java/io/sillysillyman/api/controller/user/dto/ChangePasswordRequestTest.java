package io.sillysillyman.api.controller.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @DisplayName("새 비밀번호 검증")
    @Nested
    class ValidateNewPassword {

        private static final String MESSAGE = "new password must be different from current password";

        @DisplayName("현재 비밀번호와 새 비밀번호가 다르면 성공")
        @Test
        void given_DifferentNewPassword_when_Validate_then_NoViolation() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                "password1!",
                "newPassword1!",
                "newPassword1!"
            );

            // when
            Set<ConstraintViolation<ChangePasswordRequest>> violations =
                validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .doesNotContain(MESSAGE);
        }

        @DisplayName("현재 비밀번호와 새 비밀번호가 같으면 실패")
        @Test
        void given_SameNewPassword_when_Validate_then_ReturnViolation() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                "password1!",
                "password1!",
                "password1!"
            );

            // when
            Set<ConstraintViolation<ChangePasswordRequest>> violations =
                validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .contains(MESSAGE);
        }
    }

    @DisplayName("비밀번호 확인 검증")
    @Nested
    class ValidateConfirmNewPassword {

        private static final String MESSAGE = "confirm new password must match with new password";

        @DisplayName("새 비밀번호와 새 비밀번호 확인이 같으면 성공")
        @Test
        void given_SameConfirmNewPassword_when_Validate_then_NoViolation() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                "password1!",
                "newPassword1!",
                "newPassword1!"
            );

            // when
            Set<ConstraintViolation<ChangePasswordRequest>> violations =
                validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .doesNotContain(MESSAGE);
        }

        @DisplayName("새 비밀번호와 새 비밀번호 확인이 다르면 실패")
        @Test
        void given_DifferentConfirmNewPassword_when_Validate_then_ReturnViolation() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                "password1!",
                "newPassword1!",
                "different1!"
            );

            // when
            Set<ConstraintViolation<ChangePasswordRequest>> violations =
                validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .contains(MESSAGE);
        }
    }
}
