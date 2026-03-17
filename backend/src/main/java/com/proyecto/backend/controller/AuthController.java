package com.proyecto.backend.controller;

import com.proyecto.backend.model.User;
import com.proyecto.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record RegisterRequest(String nombreCompleto, String email, String password) {}

    public record LoginRequest(String email, String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request == null
                || request.nombreCompleto() == null || request.nombreCompleto().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos"));
        }

        if (userRepository.findByEmail(request.email().trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El email ya está registrado"));
        }

        User user = new User();
        Long maxId = userRepository.findMaxId();
        user.setId((maxId == null ? 0L : maxId) + 1L);
        user.setNombreCompleto(request.nombreCompleto().trim());
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "nombreCompleto", saved.getNombreCompleto(),
                "email", saved.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null
                || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos"));
        }

        return userRepository.findByEmail(request.email().trim())
                .map(user -> {
                    String storedHash = user.getPasswordHash();
                    if (storedHash == null || storedHash.isBlank()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
                    }

                    if (!passwordEncoder.matches(request.password(), storedHash)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
                    }

                    return ResponseEntity.ok(Map.of(
                            "id", user.getId(),
                            "nombreCompleto", user.getNombreCompleto(),
                            "email", user.getEmail()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas")));
    }
}
