package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Achievement;
import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.entity.UserAchievement;
import com.abrar.BOOKSTORE.repository.AchievementRepository;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import com.abrar.BOOKSTORE.repository.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AchievementService {

    // Referenced by AchievementSeeder too - kept here since this class owns
    // the actual award logic for each one.
    public static final String CODE_FIRST_BOOK = "FIRST_BOOK";
    public static final String CODE_FIVE_BOOKS = "FIVE_BOOKS";
    public static final String CODE_FIFTEEN_BOOKS = "FIFTEEN_BOOKS";
    public static final String CODE_THREE_GENRES = "THREE_GENRES";
    public static final String CODE_ONE_SITTING = "ONE_SITTING";

    @Autowired
    private AchievementRepository achievementRepository;
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    @Autowired
    private ReadingProgressRepository readingProgressRepository;

    /**
     * Call right after marking a ReadingProgress as finished. Checks every
     * book-completion-based achievement and awards any the user has newly
     * qualified for.
     *
     * @return achievements newly earned by this specific completion (empty if
     *         none, e.g. re-checking an already-decorated milestone).
     */
    public List<Achievement> checkBookCompletionAchievements(User user, ReadingProgress justFinished) {
        List<Achievement> newlyEarned = new ArrayList<>();
        List<ReadingProgress> finished = readingProgressRepository.findByUserOrderByLastReadAtDesc(user).stream()
                .filter(ReadingProgress::isFinished).toList();
        int finishedCount = finished.size();

        tryAward(user, CODE_FIRST_BOOK, finishedCount >= 1, newlyEarned);
        tryAward(user, CODE_FIVE_BOOKS, finishedCount >= 5, newlyEarned);
        tryAward(user, CODE_FIFTEEN_BOOKS, finishedCount >= 15, newlyEarned);

        Set<Long> distinctGenreIds = new HashSet<>();
        for (ReadingProgress rp : finished) {
            for (Genre g : rp.getBook().getGenres()) {
                distinctGenreIds.add(g.getId());
            }
        }
        tryAward(user, CODE_THREE_GENRES, distinctGenreIds.size() >= 3, newlyEarned);

        boolean finishedInOneSitting = justFinished.getFinishedAt() != null
                && Duration.between(justFinished.getStartedAt(), justFinished.getFinishedAt()).toMinutes() <= 15;
        tryAward(user, CODE_ONE_SITTING, finishedInOneSitting, newlyEarned);

        return newlyEarned;
    }

    private void tryAward(User user, String code, boolean qualifies, List<Achievement> newlyEarned) {
        if (!qualifies || userAchievementRepository.existsByUserAndAchievement_Code(user, code)) {
            return;
        }
        Achievement achievement = achievementRepository.findByCode(code).orElse(null);
        if (achievement == null) {
            return; // Seeder hasn't run yet / codes out of sync - fail quiet, not loud.
        }
        userAchievementRepository.save(new UserAchievement(user, achievement));
        newlyEarned.add(achievement);
    }

    /** All achievements the user has earned, most recent first. */
    public List<UserAchievement> getEarnedAchievements(User user) {
        return userAchievementRepository.findByUserOrderByEarnedAtDesc(user);
    }
}