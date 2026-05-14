package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String empId;

    private String name;

    @Column(unique = true)
    private String email;

    private String mobileNo;
    private String passwordHash;

    @Column(nullable = false)
    private boolean isFirstLogin = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "batch_code", referencedColumnName = "batch_code", nullable = true)
    private Cohort cohort;
}
