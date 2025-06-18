package com.bob.smash.repository;

import com.bob.smash.entity.PartnerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerInfoRepository extends JpaRepository<PartnerInfo, String> {
    Optional<PartnerInfo> findByMemberEmailId(String emailId); // 이메일로 파트너 정보 찾기
    void deleteByMemberEmailId(String email); // 이메일로 파트너 정보 삭제
}