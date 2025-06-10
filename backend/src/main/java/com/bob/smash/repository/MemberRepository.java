package com.bob.smash.repository;

import com.bob.smash.entity.Member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository <Member, String> {
  Optional<Member> findByEmailId(String emailId); //임시 request test용
}
