package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService service;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public UserController(UserService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate(); // ✅ SIMPLE FIX
    }

    @GetMapping("/name/{id}")
    public ResponseEntity<String> getUserName(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getName()))
                .orElse(ResponseEntity.ok("Unknown User"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        long userCount = userRepository.count();
        Integer questionCount = 0;

        try {
            // ✅ CALL OTHER SERVICE USING RestTemplate
            questionCount = restTemplate.getForObject(
                    "http://3.110.167.15:8082/questions/count",
                    Integer.class
            );
        } catch (Exception e) {
            System.err.println("Question Service unreachable: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "userCount", userCount,
                "questionCount", questionCount != null ? questionCount : 0,
                "answerCount", 0
        ));
    }

    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}