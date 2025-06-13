package com.bob.smash.dto;

import com.bob.smash.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileDTO {
    private String email;
    private String nickname;
    private Member.LoginType loginType;
    private boolean isPartner;
    private String profileImageUrl; // path + sName 조합
}