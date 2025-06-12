package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileImage {
    @Id
    @Column(length = 100)
    private String memberId;
    
    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "s_name", length = 150, nullable = false, unique = true)
    private String sName;

    @Column(name = "o_name", length = 255, nullable = false)
    private String oName;

    @Column(length = 500, nullable = false)
    private String path;

    @Column(length = 10, nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer size;
}