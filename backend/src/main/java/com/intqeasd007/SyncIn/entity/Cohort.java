package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cohorts")
public class Cohort {

    @Id
    @Column(name = "batch_code", nullable = false, unique = true)
    private String batchCode;

    private String trackName;

    private Long pocId;
}
