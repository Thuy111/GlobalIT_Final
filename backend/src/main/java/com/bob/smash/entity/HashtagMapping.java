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
@IdClass(HashtagMapping.PK.class)
public class HashtagMapping {
    @Id
    @ManyToOne
    @JoinColumn(name = "hashtag_idx")
    private Hashtag hashtag;

    @Id
    @ManyToOne
    @JoinColumn(name = "request_idx")
    private Request request;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Integer hashtag;
        private Integer request;
    }
}