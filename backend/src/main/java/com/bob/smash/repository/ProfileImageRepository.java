package com.bob.smash.repository;

import com.bob.smash.entity.Member;
import com.bob.smash.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository <ProfileImage, String> {
    Optional<ProfileImage> findByMember(Member member);
}
