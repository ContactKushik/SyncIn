package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.Role;
import com.intqeasd007.SyncIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByCohort_BatchCode(String batchCode);
    long countByCohort_BatchCodeAndRole(String batchCode, Role role);
}
