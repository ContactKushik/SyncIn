package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CohortRepository extends JpaRepository<Cohort, String> {
    List<Cohort> findByPocEmpId(String pocEmpId);
}
