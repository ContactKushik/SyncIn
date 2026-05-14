package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long internId;
    private String reason;
    private String description;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
}

