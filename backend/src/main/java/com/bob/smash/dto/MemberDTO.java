package com.bob.smash.dto;

import java.time.LocalDateTime;

import com.bob.smash.entity.Member.LoginType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
  private String emailId;
    private String nickname;
    // private String name;
    private LocalDateTime createdAt;
    private LoginType loginType;
    private byte role;
    private String tel;
    private String region;
}
