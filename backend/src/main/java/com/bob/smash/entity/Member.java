package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Member {
    @Id
    @Column(name = "email_id", length = 100)
    private String emailId;

    @Column(length = 30, nullable = false, unique = true)
    private String nickname;

    // @Column(length = 30, nullable = false)
    // private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 10)
    private LoginType loginType;

    @Column(nullable = false)
    private byte role;

    @Column(length = 15)
    private String tel;

    @Column(length = 50)
    private String region;

    public enum LoginType {
        google, kakao
    }

    public void changeNickname(String nickname) {this.nickname = nickname;}
    public void changeTel(String tel) {this.tel = tel;}
    public void changeRegion(String region) {this.region = region;}
}