
package com.bob.smash.repository;

import com.bob.smash.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    Page<Request> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
