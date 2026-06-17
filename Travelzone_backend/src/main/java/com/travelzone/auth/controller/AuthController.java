package com.travelzone.auth.controller;

import com.travelzone.auth.dto.AuthResponse;
import com.travelzone.auth.dto.RegisterRequest;
import com.travelzone.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                authService.login(
                        request.get("email"),
                        request.get("password")
                )
        );
    }
}