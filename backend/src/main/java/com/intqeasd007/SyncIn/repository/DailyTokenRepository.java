package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.DailyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTokenRepository extends JpaRepository<DailyToken, Long> {
    Optional<DailyToken> findByBatchCodeAndDate(String batchCode, LocalDate date);
    Optional<DailyToken> findByToken(String token);
}
