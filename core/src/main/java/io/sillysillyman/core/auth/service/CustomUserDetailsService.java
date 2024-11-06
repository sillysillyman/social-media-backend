package io.sillysillyman.core.auth.service;

import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.core.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return new CustomUserDetails(
                userRepository.findByUsername(username).orElseThrow(() ->
                    new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
                )
            );
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
