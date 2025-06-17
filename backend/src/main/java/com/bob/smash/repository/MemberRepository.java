package com.bob.smash.repository;

import com.bob.smash.entity.Member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository <Member, String> {
  Optional<Member> findByEmailId(String emailId); // 이메일로 회원 여부 조회 메서드
  Optional<Member> findByTel(String phone); // 전화번호로 회원 여부 조회 메서드
  boolean existsByNickname(String nickname); // 닉네임 중복 확인 메서드
  void deleteByEmailId(String emailId); // 이메일로 회원 삭제 메서드
}
