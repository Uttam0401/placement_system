package com.uttam.placement_system.repository;

import com.uttam.placement_system.model.Tpo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TpoRepository extends JpaRepository<Tpo, Long> {
    Optional<Tpo> findByEmail(String email);
    boolean existsByEmail(String email);
}