package com.trace.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;
import com.trace.auth.dto.LoginRequest;
import com.trace.auth.dto.LoginResponse;
import com.trace.auth.dto.RefreshTokenRequest;
import com.trace.auth.dto.RegisterRequest;
import com.trace.auth.dto.RegistrationResponse;
import com.trace.auth.dto.TokenRefreshResponse;
import com.trace.auth.refresh.RefreshToken;
import com.trace.auth.refresh.RefreshTokenService;
import com.trace.auth.security.JwtService;
import com.trace.auth.security.TraceUserDetailsService;
import com.trace.user.entity.Role;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.RoleRepository;
import com.trace.user.repository.UserRepository;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final TraceUserDetailsService userDetailsService;
        private final AuditLogService auditLogService;

        public AuthService(
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RefreshTokenService refreshTokenService,
                        TraceUserDetailsService userDetailsService,
                        AuditLogService auditLogService) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
                this.userDetailsService = userDetailsService;
                this.auditLogService = auditLogService;
        }

        @Transactional
        public RegistrationResponse register(RegisterRequest request) {

                String email = request.email().trim();

                if (userRepository.existsByEmailIgnoreCase(email)) {
                        throw new EmailAlreadyExistsException(email);
                }

                Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                                .orElseThrow(() -> new IllegalStateException(
                                                "ROLE_CUSTOMER is not configured"));

                User user = new User(
                                request.name().trim(),
                                email,
                                passwordEncoder.encode(request.password()),
                                UserStatus.ACTIVE);

                user.addRole(customerRole);

                User savedUser = userRepository.save(user);

                return new RegistrationResponse(
                                savedUser.getId(),
                                savedUser.getName(),
                                savedUser.getEmail(),
                                "Registration successful");
        }

        @Transactional
        public LoginResponse login(LoginRequest request) {

                String email = request.email().trim();

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                email,
                                                request.password()));

                org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication
                                .getPrincipal();

                String accessToken = jwtService.generateToken(userDetails);

                User user = userRepository.findByEmailIgnoreCase(email)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Authenticated user no longer exists"));

                auditLogService.record(
                                user.getId(),
                                AuditAction.USER_LOGIN,
                                "User",
                                user.getId(),
                                null,
                                "LOGIN_SUCCESS",
                                null);

                String refreshToken = refreshTokenService.createToken(user);

                return new LoginResponse(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                jwtService.getExpirationMillis());
        }

        @Transactional
        public TokenRefreshResponse refresh(RefreshTokenRequest request) {

                RefreshToken currentToken = refreshTokenService.validateToken(request.refreshToken());

                User user = currentToken.getUser();

                currentToken.revoke();

                String accessToken = jwtService.generateToken(
                                userDetailsService.loadUserByUsername(user.getEmail()));

                String newRefreshToken = refreshTokenService.createToken(user);

                return new TokenRefreshResponse(
                                accessToken,
                                newRefreshToken,
                                "Bearer",
                                jwtService.getExpirationMillis());
        }
}
