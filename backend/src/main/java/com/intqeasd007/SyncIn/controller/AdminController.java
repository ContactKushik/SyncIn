package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.CreateUserRequest;
import com.intqeasd007.SyncIn.entity.Role;
import com.intqeasd007.SyncIn.entity.User;
import com.intqeasd007.SyncIn.repository.UserRepository;
import com.intqeasd007.SyncIn.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final String ADMIN_USERNAME = "sudo";
    private static final String ADMIN_PASSWORD = "racingcar123";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            String token = jwtUtil.generateToken(0L, "ADMIN");
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid admin credentials"));
    }

    @PostMapping("/create-poc")
    public ResponseEntity<?> createPoc(
            HttpServletRequest httpRequest,
            @RequestBody CreateUserRequest request) {

        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        User user = new User();
        user.setEmpId(request.getEmpId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setPasswordHash(request.getPasswordHash());
        user.setRole(Role.POC);
        user.setFirstLogin(true);

        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "userId", saved.getUserId(),
                "empId", saved.getEmpId(),
                "name", saved.getName(),
                "role", saved.getRole().name()
        ));
    }
}
