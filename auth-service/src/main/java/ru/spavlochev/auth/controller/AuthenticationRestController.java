package ru.spavlochev.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.spavlochev.auth.openapi.api.AuthenticationApi;
import ru.spavlochev.auth.openapi.dto.AuthResponseDto;
import ru.spavlochev.auth.openapi.dto.LoginRequestDto;
import ru.spavlochev.auth.openapi.dto.RegisterRequestDto;
import ru.spavlochev.auth.service.AuthService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthenticationRestController implements AuthenticationApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponseDto> login(LoginRequestDto loginRequestDto) {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> register(RegisterRequestDto registerRequestDto) {
        authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> validate(String authHeader, String cookieToken) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            authHeader = "Bearer " + cookieToken;
        }

        try {
            String token = authHeader.substring(7);
            UUID userId = authService.validateToken(token);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Id", userId.toString());

            return ResponseEntity.ok().headers(headers).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
