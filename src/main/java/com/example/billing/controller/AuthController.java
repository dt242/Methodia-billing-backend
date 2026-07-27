package com.example.billing.controller;

import com.example.billing.dto.AuthRequest;
import com.example.billing.dto.AuthResponse;
import com.example.billing.model.User;
import com.example.billing.repository.UserRepository;
import com.example.billing.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                          JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.reference(), request.password())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Грешно потребителско име или парола");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.reference());
        User user = userRepository.findByReference(request.reference()).orElseThrow();

        final String jwt = jwtUtil.generateToken(userDetails, user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(jwt, user.getRole().name()));
    }
}