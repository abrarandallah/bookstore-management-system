package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import lombok.Getter;

// Achievement definitions are static, seeded once at startup by
// AchievementSeeder (see that class for the actual list) - this table just
// holds the catalog; UserAchievement records who's actually earned one.
@Getter
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Stable identifier used in code to award/check for a specific
    // achievement (see AchievementService) - never shown to the user.
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    // A single emoji, rendered directly - avoids needing an icon asset
    // pipeline for something this small.
    @Column(nullable = false)
    private String icon;

    public Achievement() {
    }

    public Achievement(String code, String name, String description, String icon) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }
}