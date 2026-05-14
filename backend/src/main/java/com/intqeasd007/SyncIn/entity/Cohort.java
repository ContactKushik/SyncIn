package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cohorts")
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cohortId;

    private String batchCode;

    private String trackName;

    private Long pocId;
}

