package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.LoginRequest;
import com.intqeasd007.SyncIn.dto.LoginResponse;
import com.intqeasd007.SyncIn.entity.User;
import com.intqeasd007.SyncIn.repository.UserRepository;
import com.intqeasd007.SyncIn.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> optUser = userRepository.findByEmpId(request.getEmpId());

        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = optUser.get();

        // Plain comparison (no bcrypt in Sprint 1)
        if (!user.getPasswordHash().equals(request.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().name());

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getRole().name(),
                user.isFirstLogin(),
                user.getUserId()
        ));
    }
}

