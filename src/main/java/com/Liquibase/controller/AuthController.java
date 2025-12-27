package com.Liquibase.controller;

import com.Liquibase.entity.UserEntity;
import com.Liquibase.repository.UserRepository;
import com.Liquibase.utils.JwtUtil;
import com.Liquibase.utils.TokenBlacklist;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist blacklist;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            TokenBlacklist blacklist,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔐 LOGIN
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.get("username"),
                                request.get("password")
                        )
                );

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        return Map.of(
                "token", token,
                "type", "Bearer",
                "expiresIn", 1800
        );
    }

    // 🆕 REGISTER (MULTI-ROLE SUPPORT)
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> request) {

        String username = (String) request.get("username");
        String password = (String) request.get("password");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.get("roles");

        if (username == null || password == null) {
            throw new RuntimeException("Username and password are required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Default role if none provided
        if (roles == null || roles.isEmpty()) {
            roles = List.of("USER");
        }

        // ✅ OPTIONAL: ROLE VALIDATION (recommended)
        List<String> allowedRoles = List.of("USER", "ADMIN");

        for (String role : roles) {
            if (!allowedRoles.contains(role)) {
                throw new RuntimeException("Invalid role: " + role);
            }
        }

        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(roles)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        return Map.of(
                "message", "User registered successfully",
                "token", token,
                "type", "Bearer",
                "expiresIn", 1800
        );
    }


    // 🚪 LOGOUT
    @PostMapping("/logout")
    public Map<String, String> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        blacklist.blacklist(authHeader.substring(7));
        return Map.of("message", "Logged out successfully");
    }
}
