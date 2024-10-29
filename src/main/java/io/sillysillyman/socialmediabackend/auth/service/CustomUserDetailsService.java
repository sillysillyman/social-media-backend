package io.sillysillyman.socialmediabackend.auth.service;

import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.socialmediabackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return new CustomUserDetails(userService.getByUsername(username));
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
