package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.CreateCohortRequest;
import com.intqeasd007.SyncIn.dto.OnboardInternRequest;
import com.intqeasd007.SyncIn.entity.Cohort;
import com.intqeasd007.SyncIn.entity.Role;
import com.intqeasd007.SyncIn.entity.User;
import com.intqeasd007.SyncIn.repository.CohortRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/poc")
public class POCController {

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private UserRepository userRepository;

    // ── helper: assert POC role ──
    private Long requirePoc(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"POC".equals(role)) return null;
        return (Long) req.getAttribute("userId");
    }

    // ───────────── COHORT CRUD ─────────────

    @PostMapping("/cohorts")
    public ResponseEntity<?> createCohort(HttpServletRequest req,
                                          @RequestBody CreateCohortRequest body) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        if (cohortRepository.existsById(body.getBatchCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Batch code already exists"));
        }

        Cohort cohort = new Cohort();
        cohort.setBatchCode(body.getBatchCode());
        cohort.setTrackName(body.getTrackName());
        cohort.setPocId(pocId);

        Cohort saved = cohortRepository.save(cohort);
        return ResponseEntity.ok(Map.of(
                "batchCode", saved.getBatchCode(),
                "trackName", saved.getTrackName()
        ));
    }

    @GetMapping("/cohorts")
    public ResponseEntity<?> getMyCohorts(HttpServletRequest req) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        List<Cohort> cohorts = cohortRepository.findByPocId(pocId);
        return ResponseEntity.ok(cohorts);
    }

    // ───────────── ONBOARD INTERN ─────────────

    @PostMapping("/interns")
    public ResponseEntity<?> onboardIntern(HttpServletRequest req,
                                           @RequestBody OnboardInternRequest body) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        // Verify cohort belongs to this POC
        Cohort cohort = cohortRepository.findById(body.getBatchCode()).orElse(null);
        if (cohort == null || !cohort.getPocId().equals(pocId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cohort"));
        }

        // Generate random 8-char temp password
        String plainTextPassword = generateTempPassword(8);

        // *** CRITICAL: Log the unhashed password for developer testing ***
        log.info("GENERATED TEMP PASSWORD FOR {}: {}", body.getEmail(), plainTextPassword);

        User user = new User();
        user.setEmpId(body.getEmpId());
        user.setName(body.getName());
        user.setEmail(body.getEmail());
        user.setMobileNo(body.getMobileNo());
        user.setPasswordHash(plainTextPassword); // stored as plain text (Sprint 1 style)
        user.setRole(Role.INTERN);
        user.setFirstLogin(true);
        user.setCohort(cohort);

        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "userId", saved.getUserId(),
                "empId", saved.getEmpId(),
                "name", saved.getName(),
                "role", saved.getRole().name(),
                "batchCode", cohort.getBatchCode(),
                "tempPassword", plainTextPassword
        ));
    }

    // ───────────── GET INTERNS BY COHORT ─────────────

    @GetMapping("/cohorts/{batchCode}/interns")
    public ResponseEntity<?> getInternsByCohort(HttpServletRequest req,
                                                @PathVariable String batchCode) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Cohort cohort = cohortRepository.findById(batchCode).orElse(null);
        if (cohort == null || !cohort.getPocId().equals(pocId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cohort"));
        }

        List<User> interns = userRepository.findByCohort_BatchCode(batchCode);
        List<Map<String, Object>> result = interns.stream().map(u -> Map.<String, Object>of(
                "userId", u.getUserId(),
                "empId", u.getEmpId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "mobileNo", u.getMobileNo(),
                "role", u.getRole().name()
        )).toList();

        return ResponseEntity.ok(result);
    }

    // ───────────── PROMOTE INTERN → CR ─────────────

    @PutMapping("/interns/{userId}/promote")
    public ResponseEntity<?> promoteInternToCR(HttpServletRequest req,
                                               @PathVariable Long userId) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        User intern = userRepository.findById(userId).orElse(null);
        if (intern == null || intern.getCohort() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        Cohort cohort = intern.getCohort();
        if (!cohort.getPocId().equals(pocId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (intern.getRole() == Role.CR) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is already a CR"));
        }

        long crCount = userRepository.countByCohort_BatchCodeAndRole(cohort.getBatchCode(), Role.CR);
        if (crCount >= 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Maximum 2 CRs allowed per cohort"));
        }

        intern.setRole(Role.CR);
        userRepository.save(intern);

        return ResponseEntity.ok(Map.of(
                "userId", intern.getUserId(),
                "empId", intern.getEmpId(),
                "name", intern.getName(),
                "role", "CR"
        ));
    }

    // ───────────── DEMOTE CR → INTERN ─────────────

    @PutMapping("/interns/{userId}/demote")
    public ResponseEntity<?> demoteCRToIntern(HttpServletRequest req,
                                              @PathVariable Long userId) {
        Long pocId = requirePoc(req);
        if (pocId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCohort() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        Cohort cohort = user.getCohort();
        if (!cohort.getPocId().equals(pocId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (user.getRole() != Role.CR) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a CR"));
        }

        user.setRole(Role.INTERN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "empId", user.getEmpId(),
                "name", user.getName(),
                "role", "INTERN"
        ));
    }

    // ── password generator ──
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
