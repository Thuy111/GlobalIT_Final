package com.bob.smash.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.smash.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    Optional<Hashtag> findByTag(String tag);
}
