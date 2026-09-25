package ru.spavlochev.auth.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.spavlochev.auth.entity.AppUser;
import ru.spavlochev.auth.openapi.dto.AuthResponseDto;
import ru.spavlochev.auth.openapi.dto.LoginRequestDto;
import ru.spavlochev.auth.openapi.dto.RegisterRequestDto;
import ru.spavlochev.auth.repository.UserRepository;
import ru.spavlochev.auth.security.JwtUtil;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OutboxService outboxService;

    @Transactional
    public void register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("Email already exists");
        }

        var user = userRepository.save(AppUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build());

        outboxService.saveEvent("UserCreated", user.getId(), "User",
                Map.of("userId", user.getId().toString(),
                        "email", user.getEmail()));
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponseDto()
                .token(token)
                .userId(user.getId());
    }

    public UUID validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
