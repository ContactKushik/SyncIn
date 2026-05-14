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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cr")
public class CRController {

    @Autowired
    private DailyTokenRepository dailyTokenRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    // ── helper: require CR role ──
    private User requireCR(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"CR".equals(role)) return null;
        String empId = (String) req.getAttribute("empId");
        return userRepository.findById(empId).orElse(null);
    }

    // ───────────── GENERATE DAILY QR TOKEN ─────────────

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateDailyToken(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        // Check if token already exists for today
        var existing = dailyTokenRepository.findByBatchCodeAndDate(batchCode, today);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "token", existing.get().getToken(),
                    "batchCode", batchCode,
                    "date", today.toString(),
                    "message", "Token already generated for today"
            ));
        }

        // Generate new token
        String token = UUID.randomUUID().toString();
        DailyToken dailyToken = new DailyToken();
        dailyToken.setBatchCode(batchCode);
        dailyToken.setDate(today);
        dailyToken.setToken(token);
        dailyTokenRepository.save(dailyToken);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "batchCode", batchCode,
                "date", today.toString()
        ));
    }

    // ───────────── GET TODAY'S TOKEN ─────────────

    @GetMapping("/today-token")
    public ResponseEntity<?> getTodayToken(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        var existing = dailyTokenRepository.findByBatchCodeAndDate(batchCode, today);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "token", existing.get().getToken(),
                    "batchCode", batchCode,
                    "date", today.toString()
            ));
        }

        return ResponseEntity.ok(Map.of("token", "", "batchCode", batchCode, "date", today.toString()));
    }

    // ───────────── GET PRESENT INTERNS TODAY (for defaulter marking) ─────────────

    @GetMapping("/present-today")
    public ResponseEntity<?> getPresentToday(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        List<Attendance> presentList = attendanceRepository
                .findByUser_Cohort_BatchCodeAndDateAndStatus(batchCode, today, AttendanceStatus.PRESENT);

        List<Map<String, Object>> result = presentList.stream().map(a -> Map.<String, Object>of(
                "attendanceId", a.getAttendanceId(),
                "empId", a.getUser().getEmpId(),
                "name", a.getUser().getName(),
                "status", a.getStatus().name()
        )).toList();

        return ResponseEntity.ok(result);
    }

    // ───────────── MARK DEFAULTER (PRESENT → ABSENT) ─────────────

    @PutMapping("/mark-defaulter/{attendanceId}")
    public ResponseEntity<?> markDefaulter(HttpServletRequest req,
                                           @PathVariable Long attendanceId) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
        if (attendance == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Attendance record not found"));
        }

        // Only allow for current date
        if (!attendance.getDate().equals(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Can only modify today's attendance"));
        }

        // Cannot modify if status is AL or UL (locked by POC)
        if (attendance.getStatus() == AttendanceStatus.AL || attendance.getStatus() == AttendanceStatus.UL) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot modify: status is locked (AL/UL)"));
        }

        // Only mark PRESENT as ABSENT
        if (attendance.getStatus() != AttendanceStatus.PRESENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Can only mark PRESENT as defaulter"));
        }

        // Verify the attendance belongs to the CR's cohort
        if (!attendance.getUser().getCohort().getBatchCode().equals(cr.getCohort().getBatchCode())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        attendance.setStatus(AttendanceStatus.ABSENT);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok(Map.of(
                "attendanceId", attendance.getAttendanceId(),
                "empId", attendance.getUser().getEmpId(),
                "name", attendance.getUser().getName(),
                "status", "ABSENT",
                "message", "Marked as defaulter"
        ));
    }
}
