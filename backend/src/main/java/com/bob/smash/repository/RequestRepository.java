// RequestRepository.java
package com.bob.smash.repository;

import com.bob.smash.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Integer> {
  //save(), findById(), findAll(), deleteById()..
}
