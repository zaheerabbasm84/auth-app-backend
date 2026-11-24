package com.gen.auth.security;

import com.gen.auth.apiexception.ApiException;
import com.gen.auth.apiexception.ErrorCodes;
import com.gen.auth.model.User;
import com.gen.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailId(username).orElseThrow(() -> new ApiException("Invalid Email Id or Password", HttpStatus.NOT_FOUND, new IllegalStateException("Invalid Email Id or Password")).addErrors(ErrorCodes.INVALID_EMAIL_ID_OR_PASSWORD));
        return new User();
    }
}
