package com.bob.smash.repository;

import com.bob.smash.entity.PartnerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerInfoRepository extends JpaRepository<PartnerInfo, String> {
    Optional<PartnerInfo> findByMemberEmailId(String emailId); // 이메일로 파트너 정보 찾기
    String findBnoByMember_EmailId(String emailId); // 이메일로 bno 조회
    void deleteByMember_EmailId(String email); // 이메일로 파트너 정보 삭제
}