package com.bob.smash.repository;

import com.bob.smash.entity.PartnerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerInfoRepository extends JpaRepository<PartnerInfo, String> {
    Optional<PartnerInfo> findByMember_EmailId(String emailId); // 이메일로 파트너 정보 찾기
    void deleteByMember_EmailId(String email); // 이메일로 파트너 정보 삭제
    // 이메일로 bno 조회
    @Query("SELECT p.bno FROM PartnerInfo p WHERE p.member.emailId = :emailId")
    String findBnoByMember_EmailId(@Param("emailId") String emailId);
}