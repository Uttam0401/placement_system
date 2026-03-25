package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByIdentifierAndOtpTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String identifier, String otpType);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType")
    void deleteByIdentifierAndOtpType(@Param("identifier") String identifier,
                                      @Param("otpType") String otpType);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}