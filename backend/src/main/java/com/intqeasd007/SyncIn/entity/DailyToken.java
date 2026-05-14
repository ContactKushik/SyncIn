package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_tokens", uniqueConstraints = @UniqueConstraint(columnNames = {"batch_code", "date"}))
public class DailyToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", nullable = false)
    private String batchCode;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, unique = true)
    private String token;
}
