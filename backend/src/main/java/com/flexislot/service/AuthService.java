package com.flexislot.service;

import com.flexislot.domain.User;
import com.flexislot.domain.enums.UserRole;
import com.flexislot.dto.auth.AuthResponse;
import com.flexislot.dto.auth.LoginRequest;
import com.flexislot.dto.auth.RegisterRequest;
import com.flexislot.exception.AppException;
import com.flexislot.repository.UserRepository;
import com.flexislot.security.JwtTokenProvider;
import com.flexislot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already registered", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();
        user = userRepository.save(user);
        log.info("Registered user: {}", user.getEmail());
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal.getId(), principal.getEmail(), principal.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(principal.getId())
                .role(principal.getRole())
                .build();
    }
}
