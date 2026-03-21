package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Company> findByApprovalStatus(Company.ApprovalStatus status);
    long countByApprovalStatus(Company.ApprovalStatus status);
}