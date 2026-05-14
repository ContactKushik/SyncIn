package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmpId(String empId);
}

