package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.entity.*;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.DailyTokenRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/intern")
public class InternController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DailyTokenRepository dailyTokenRepository;

    // ── helper ──
    private User requireInternOrCR(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"INTERN".equals(role) && !"CR".equals(role)) return null;
        String empId = (String) req.getAttribute("empId");
        return userRepository.findById(empId).orElse(null);
    }

    // ───────────── PUNCH IN (validate QR token) ─────────────

    @PostMapping("/punch-in")
    public ResponseEntity<?> punchIn(HttpServletRequest req,
                                     @RequestBody Map<String, String> body) {
        User user = requireInternOrCR(req);
        if (user == null || user.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        LocalDate today = LocalDate.now();

        // Check if already scanned today
        var existing = attendanceRepository.findByUser_EmpIdAndDate(user.getEmpId(), today);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Already punched in today",
                    "status", existing.get().getStatus().name()
            ));
        }

        // Validate the token belongs to their cohort and today's date
        var dailyToken = dailyTokenRepository.findByToken(token).orElse(null);
        if (dailyToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR token"));
        }
        if (!dailyToken.getBatchCode().equals(user.getCohort().getBatchCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token does not match your cohort"));
        }
        if (!dailyToken.getDate().equals(today)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token has expired (not today's token)"));
        }

        // Mark attendance
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok(Map.of(
                "message", "Punch-in successful!",
                "status", "PRESENT",
                "date", today.toString()
        ));
    }

    // ───────────── GET TODAY'S STATUS ─────────────

    @GetMapping("/today-status")
    public ResponseEntity<?> getTodayStatus(HttpServletRequest req) {
        User user = requireInternOrCR(req);
        if (user == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        LocalDate today = LocalDate.now();
        var existing = attendanceRepository.findByUser_EmpIdAndDate(user.getEmpId(), today);

        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", existing.get().getStatus().name(),
                    "date", today.toString(),
                    "punchedIn", true
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "NOT_MARKED",
                "date", today.toString(),
                "punchedIn", false
        ));
    }
}
