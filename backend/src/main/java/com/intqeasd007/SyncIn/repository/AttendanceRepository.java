package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.Attendance;
import com.intqeasd007.SyncIn.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUser_EmpIdAndDate(String empId, LocalDate date);
    List<Attendance> findByUser_Cohort_BatchCodeAndDate(String batchCode, LocalDate date);
    List<Attendance> findByUser_Cohort_BatchCodeAndDateAndStatus(String batchCode, LocalDate date, AttendanceStatus status);
}
