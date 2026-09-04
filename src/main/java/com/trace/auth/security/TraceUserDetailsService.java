package com.trace.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class TraceUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public TraceUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .toArray(String[]::new)
                )
                .disabled(user.getStatus().name().equals("SUSPENDED"))
                .build();
    }
}