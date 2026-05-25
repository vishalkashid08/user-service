package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

	@Autowired
	private UserRepository repository; // Correct

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // Register User
    public User registerUser(User user) {
        // Check duplicate email using the Optional correctly
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user.setRole("USER"); // or "ROLE_USER" depending on your security config
        return repository.save(user);
    }

    // Create User (Admin use)
    public User createUser(User user) {
        return repository.save(user);
    }

    // Get All Users
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    // Get User By ID
    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Delete User
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}