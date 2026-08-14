package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByCode(String code);

    boolean existsByCode(String code);
}