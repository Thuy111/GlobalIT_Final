package com.bob.smash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.HashtagMapping;
import com.bob.smash.entity.Request;

public interface HashtagMappingRepository extends JpaRepository<HashtagMapping, HashtagMapping.PK> {

    // 특정 Request에 대한 모든 HashtagMapping 조회
    List<HashtagMapping> findByRequest(Request request);

    // 의뢰서 idx로 관련 해시태그 매핑 전체 삭제
    void deleteByRequest_Idx(Integer requestIdx);

    // 하나의 의뢰서에 연결된 해시태그 목록 조회
    @Query("SELECT hm.hashtag FROM HashtagMapping hm WHERE hm.request.idx = :requestIdx")
    List<Hashtag> findHashtagsByRequestIdx(@Param("requestIdx") Integer requestIdx);

    // N+1 문제 해결용: 해시태그 즉시 로딩(JOIN FETCH)
    @Query("SELECT hm FROM HashtagMapping hm JOIN FETCH hm.hashtag WHERE hm.request.idx IN :requestIdxs")
    List<HashtagMapping> findAllByRequestIdxInWithHashtag(@Param("requestIdxs") List<Integer> requestIdxs);
    // // N+1 문제 해결용: 해시태그 즉시 로딩(JOIN FETCH)
    @Query("""
        SELECT hm
        FROM HashtagMapping hm
        JOIN FETCH hm.hashtag h
        JOIN hm.request r
        WHERE r.createdAt IS NOT NULL
        ORDER BY r.createdAt DESC
    """)
    List<HashtagMapping> findAllWithHashtagOrderByCreatedAt();
}
