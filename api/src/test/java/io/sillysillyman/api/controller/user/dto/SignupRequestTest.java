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

class SignupRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @DisplayName("비밀번호 확인 검증")
    @Nested
    class ValidateConfirmPassword {

        private static final String MESSAGE = "confirm password must match with password";

        @DisplayName("비밀번호와 비밀번호 확인이 같으면 성공")
        @Test
        void given_SameConfirmPassword_when_Validate_then_NoViolation() {
            // given
            SignupRequest request = new SignupRequest(
                "username",
                "password1!",
                "password1!"
            );

            // when
            Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .doesNotContain(MESSAGE);
        }

        @DisplayName("비밀번호와 비밀번호 확인이 다르면 실패")
        @Test
        void given_DifferentConfirmPassword_when_Validate_then_ReturnViolation() {
            // given
            SignupRequest request = new SignupRequest(
                "username",
                "password1!",
                "different1!"
            );

            // when
            Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                .extracting("message")
                .contains(MESSAGE);
        }
    }
}

