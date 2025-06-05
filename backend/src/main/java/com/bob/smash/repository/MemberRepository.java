package com.bob.smash.repository;

import com.bob.smash.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository <Member, String> {}
