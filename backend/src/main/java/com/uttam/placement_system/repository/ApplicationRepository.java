package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);
    List<Application> findByJobId(Long jobId);
    List<Application> findByJobCompanyId(Long companyId);
    Optional<Application> findByStudentIdAndJobId(Long studentId, Long jobId);
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    long countByStatus(Application.ApplicationStatus status);
    long countByStudentIdAndStatus(Long studentId, Application.ApplicationStatus status);

    @Query("SELECT COUNT(DISTINCT a.student.id) FROM Application a WHERE a.status = 'PLACED'")
    long countPlacedStudents();

    @Query("SELECT a FROM Application a WHERE a.job.company.id = :companyId " +
            "AND (:minCgpa IS NULL OR a.student.cgpa >= :minCgpa) " +
            "AND (:branch IS NULL OR a.student.branch = :branch)")
    List<Application> filterByCompany(@Param("companyId") Long companyId,
                                      @Param("minCgpa") Double minCgpa,
                                      @Param("branch") String branch);

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT a.student.branch, COUNT(a) FROM Application a WHERE a.status = 'PLACED' GROUP BY a.student.branch")
    List<Object[]> countPlacedByBranch();
}