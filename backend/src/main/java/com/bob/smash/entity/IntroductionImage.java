package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IntroductionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne
    @JoinColumn(name = "partner_bno", nullable = false)
    private PartnerInfo partnerInfo;

    @Column(name = "is_main")
    private Byte isMain;

	  @Column(name = "order_index")
    private Integer orderIndex;

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