package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.AuthRequest;
import com.example.bookingsystem.dto.AuthResponse;
import com.example.bookingsystem.security.CustomUserDetails;
import com.example.bookingsystem.security.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Operation(summary = "Authenticate a user and receive a JWT access token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
       
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String role = principal.getUser().getRole().name();
        String token = jwtUtil.generateToken(principal.getUsername(), role);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(principal.getUsername())
                .role(role)
                .expiresInMillis(jwtUtil.getExpirationMillis())
                .build();

        return ResponseEntity.ok(response);
    }
}
