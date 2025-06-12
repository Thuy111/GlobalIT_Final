package com.bob.smash.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(ImageMapping.PK.class)
public class ImageMapping {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 10, nullable = false)
    private TargetType targetType;

    @Id
    @Column(name = "target_idx", nullable = false)
    private Integer targetIdx;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_idx", nullable = false)
    private Image image;

    public enum TargetType {
        request, estimate, review
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private TargetType targetType;
        private Integer targetIdx;
        private Integer image;
    }
}