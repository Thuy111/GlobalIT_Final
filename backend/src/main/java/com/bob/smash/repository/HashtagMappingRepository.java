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

  @Query("SELECT hm.hashtag FROM HashtagMapping hm WHERE hm.request.idx = :requestIdx")
  List<Hashtag> findHashtagsByRequestIdx(@Param("requestIdx") Integer requestIdx);
}
