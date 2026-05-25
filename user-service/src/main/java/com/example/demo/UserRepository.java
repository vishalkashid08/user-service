package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring will automatically implement these for you
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}