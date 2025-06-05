package com.bob.smash.repository;

import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerInfoRepository extends JpaRepository<PartnerInfo, String> {
    Optional<PartnerInfo> findByMember(Member member);
}