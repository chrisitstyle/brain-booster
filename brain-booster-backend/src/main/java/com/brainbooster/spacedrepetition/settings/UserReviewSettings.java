package com.brainbooster.spacedrepetition.settings;

import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_review_settings")
public class UserReviewSettings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "desired_retention", nullable = false)
    private double desiredRetention = 0.90;

    @Builder.Default
    @Column(name = "new_cards_per_day", nullable = false)
    private int newCardsPerDay = 20;

    @Builder.Default
    @Column(name = "review_limit_per_day", nullable = false)
    private int reviewLimitPerDay = 200;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone = "UTC";

    @Column(name = "fsrs_parameters", columnDefinition = "text")
    private String fsrsParameters;

    @Builder.Default
    @Column(name = "algorithm_version", nullable = false, length = 20)
    private String algorithmVersion = "FSRS-6";
}
