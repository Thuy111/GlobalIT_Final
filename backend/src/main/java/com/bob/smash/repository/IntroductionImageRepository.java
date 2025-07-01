package com.bob.smash.repository;

import com.bob.smash.entity.IntroductionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IntroductionImageRepository extends JpaRepository<IntroductionImage, Long>{
    List<IntroductionImage> findByPartnerInfo_Bno(String bno);
}
