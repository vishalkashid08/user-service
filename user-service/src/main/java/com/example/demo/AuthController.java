package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.LoginRequest;
import com.example.demo.security.JwtUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. Find user by email
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);

            if (user == null) {
                System.out.println("DEBUG: Login failed - User not found: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }

            // 2. --- AUTO-UPGRADE PLAIN TEXT PASSWORDS ---
            // BCrypt hashes always start with "$2a$" or "$2b$". If it doesn't, it's plain text.
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
                System.out.println("DEBUG: Plain-text password detected for " + user.getEmail() + ". Attempting upgrade...");
                
                // Check if the typed password matches the plain-text DB password
                if (user.getPassword().equals(request.getPassword())) {
                    // It's a match! Hash it and save it back to the database
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    userRepository.save(user);
                    System.out.println("DEBUG: Successfully upgraded password to BCrypt for " + user.getEmail());
                } else {
                    System.out.println("DEBUG: Plain-text password mismatch for " + user.getEmail());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
                }
            } else {
                // 3. --- STANDARD BCRYPT CHECK ---
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    System.out.println("DEBUG: Login failed for " + request.getEmail() + " - Incorrect BCrypt password.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
                }
            }

            // 4. Generate JWT Token
            String token = jwtUtil.generateToken(user.getEmail());
            System.out.println("DEBUG: Login successful for " + user.getEmail());

            return ResponseEntity.ok(Map.of(
                "token", token,
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole()
            ));
        } catch (Exception e) {
            System.err.println("DEBUG: Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An internal error occurred"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("DEBUG: Incoming user = " + user.getEmail());

            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "User Registered Successfully"));

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 THIS WILL SHOW REAL ERROR
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

@RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
public ResponseEntity<?> handleOptions() {
    return ResponseEntity.ok().build();
}
}