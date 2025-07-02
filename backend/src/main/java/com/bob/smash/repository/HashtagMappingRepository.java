package com.bob.smash.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.HashtagMapping;
import com.bob.smash.entity.Request;

public interface HashtagMappingRepository extends JpaRepository<HashtagMapping, HashtagMapping.PK> {
  List<HashtagMapping> findByRequest(Request request);

  void deleteByRequest_Idx(Integer requestIdx); // 의뢰서 : 관련 해시태그 매핑 삭제

  @Query("SELECT hm.hashtag FROM HashtagMapping hm WHERE hm.request.idx = :requestIdx")
  List<Hashtag> findHashtagsByRequestIdx(@Param("requestIdx") Integer requestIdx);

  @Query("SELECT hm FROM HashtagMapping hm WHERE hm.request.idx IN :requestIdxs")
List<HashtagMapping> findAllByRequestIdxIn(@Param("requestIdxs") List<Integer> requestIdxs);

}
