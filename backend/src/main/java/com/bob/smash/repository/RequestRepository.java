package com.bob.smash.repository;

import com.bob.smash.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    Page<Request> findByTitleContaining(String keyword, Pageable pageable); // 🔍 검색용 추가

    List<Request> findByMember_EmailId(String email); // 이메일 : 회원의 모든 의뢰서 조회
    void deleteByMember_EmailId(String email); // 이메일 : 회원의 모든 의뢰서 삭제

    //주소
    @Query("SELECT r.useRegion FROM Request r")
    List<String> findAllUseRegions();
}
