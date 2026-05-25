package com.example.demo.dto;

import com.example.demo.User;
import com.example.demo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class authService {

    @Autowired
    private UserRepository userRepository; // Remove 'static'

    @Autowired
    private PasswordEncoder passwordEncoder; // Remove 'static'

    // Remove 'static' from this method
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Always encode passwords before saving!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }
}