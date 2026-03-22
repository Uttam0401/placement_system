package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Student> findByApprovalStatus(Student.ApprovalStatus status);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.approvalStatus = 'PENDING'")
    long countPending();
}