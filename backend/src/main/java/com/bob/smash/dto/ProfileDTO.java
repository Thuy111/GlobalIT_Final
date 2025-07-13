package com.bob.smash.dto;

import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.ProfileImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private String email;
    private String nickname;
    private Member.LoginType loginType;
    private boolean isPartner;
    private String profileImageUrl; // path + sName 조합
    private String tel;
    private String region;

    private int role;

    private String bno; // 사업자 등록 번호
    private String partnerName; // 업체명
    private String partnerTel;
    private String partnerRegion;
    private String code;

}