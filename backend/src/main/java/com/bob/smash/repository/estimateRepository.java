package com.bob.repository;

import com.bob.smash.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface estimateRepository extends JpaRepository<Estimate, Integer> {
}