package com.bob.smash.repository;

import com.bob.smash.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, Integer> {
  
}
