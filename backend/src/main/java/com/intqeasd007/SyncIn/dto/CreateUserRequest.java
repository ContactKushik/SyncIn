package com.intqeasd007.SyncIn.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String empId;
    private String name;
    private String email;
    private String mobileNo;
    private String passwordHash;
    private String role;
    private Long cohortId;
}

