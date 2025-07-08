package com.bob.smash.repository;

import com.bob.smash.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepository extends JpaRepository<Request, Integer> {
    // (검색용) 제목으로 의뢰서 조회
    Page<Request> findByTitleContaining(String keyword, Pageable pageable); 

    // (마이페이지 연동) 회원의 모든 의뢰서 조회
    List<Request> findByMember_EmailId(String emailId);
    
    // (회원 탈퇴용) 회원의 모든 의뢰서 삭제
    void deleteByMember_EmailId(String emailId);

    //주소
    @Query("SELECT r.useRegion FROM Request r")
    List<String> findAllUseRegions();

    // isDone 낙찰
    @Modifying
    @Transactional
    @Query("UPDATE Request r SET r.isDone = :isDone WHERE r.idx = :idx")
    int updateIsDone(@Param("idx") Integer idx, @Param("isDone") Byte isDone);
    // (견적서 낙찰 자동 처리용)사용 일시가 지난 의뢰서 조회
    List<Request> findByUseDateBeforeAndIsDone(LocalDateTime now, Byte isDone);

    // 종료된 의뢰서 감추기
    Page<Request> findByUseDateGreaterThanEqual(LocalDateTime now, Pageable pageable);

    // 유저가 쓴 의뢰서 개수
    int countByMember_EmailId(String memberId);

}