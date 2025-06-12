package com.bob.smash.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class ProfileDTO {
    private String emailId;
    private String nickname;
    private String loginType;
    private String profileImagePath;
    private String userType; // USER OR PARTNER
}
