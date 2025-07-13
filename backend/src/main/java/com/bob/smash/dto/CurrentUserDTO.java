package com.bob.smash.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString // ToString을 사용하여 객체의 내용을 출력할 수 있도록 설정
public class CurrentUserDTO {
    private String emailId;
    private String nickname;
    private byte role;
    private String bno; // 사업자 회원일 경우에만 존재
    private int unreadAlarm = 0;
}