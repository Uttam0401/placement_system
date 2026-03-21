package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(Job.JobStatus status);
    List<Job> findByCompanyId(Long companyId);
    List<Job> findByCompanyIdAndStatus(Long companyId, Job.JobStatus status);
    long countByStatus(Job.JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' " +
            "AND (:cgpa IS NULL OR j.minimumCgpa IS NULL OR j.minimumCgpa <= :cgpa) " +
            "AND (:branch IS NULL OR j.eligibleBranches IS NULL OR j.eligibleBranches = '' " +
            "     OR j.eligibleBranches LIKE CONCAT('%', :branch, '%'))")
    List<Job> findEligibleJobs(@Param("cgpa") Double cgpa, @Param("branch") String branch);
}