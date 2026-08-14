package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Achievement;
import com.abrar.BOOKSTORE.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Seeds the fixed catalog of achievements on every startup (unlike
// GenreSeeder/LibrarianSeeder, which only run once on an empty table) -
// checked one by one via existsByCode so adding a new achievement to
// AchievementService.CODE_* later just adds the missing row here instead of
// requiring a full reseed or migration script.
@Component
public class AchievementSeeder implements CommandLineRunner {

    @Autowired
    private AchievementRepository achievementRepository;

    @Override
    public void run(String... args) {
        seed(AchievementService.CODE_FIRST_BOOK, "First Chapter", "Finish your first book.", "\uD83D\uDCD6");
        seed(AchievementService.CODE_FIVE_BOOKS, "Bookworm", "Finish 5 books.", "\uD83D\uDC1B");
        seed(AchievementService.CODE_FIFTEEN_BOOKS, "Voracious Reader", "Finish 15 books.", "\uD83D\uDD25");
        seed(AchievementService.CODE_THREE_GENRES, "Genre Explorer", "Finish books from 3 different genres.",
                "\uD83E\uDDED");
        seed(AchievementService.CODE_ONE_SITTING, "One Sitting", "Finish a book within 15 minutes of starting it.",
                "\u26A1");
    }

    private void seed(String code, String name, String description, String icon) {
        if (!achievementRepository.existsByCode(code)) {
            achievementRepository.save(new Achievement(code, name, description, icon));
        }
    }
}