package com.flexislot.security;

import com.flexislot.domain.User;
import com.flexislot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return toPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(String userId) {
        return userRepository.findById(userId)
                .map(this::toPrincipal)
                .orElse(null);
    }

    private UserPrincipal toPrincipal(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .build();
    }
}
