package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.EmailVerificationTokenRepository;
import com.abrar.BOOKSTORE.Login.user.PasswordResetTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.repository.MyBookRepository;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import com.abrar.BOOKSTORE.repository.ReviewRepository;
import com.abrar.BOOKSTORE.repository.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MyBookRepository myBookRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired
    private ReadingProgressRepository readingProgressRepository;
    @Autowired
    private UserAchievementRepository userAchievementRepository;

    /**
     * Permanently deletes the given user's account and everything scoped to
     * it: their "my books" list, their reviews, their reading progress and
     * achievements, and any pending password-reset or email-verification
     * token. Book entities themselves are never touched -
     * a Book isn't owned by a particular user (its "author" is just a text
     * field, not a foreign key to User), so there's nothing on Book to
     * cascade.
     *
     * @throws IllegalStateException if this is the last remaining
     *                               librarian account - same protection
     *                               AdminController already applies to
     *                               role changes (see
     *                               AdminController#changeRole), so the
     *                               site can never end up with no one able
     *                               to manage it.
     */
    @Transactional
    public void deleteAccount(User user) {
        if ("ROLE_LIBRARIAN".equals(user.getRole()) && userRepository.countByRole("ROLE_LIBRARIAN") <= 1) {
            throw new IllegalStateException(
                    "Can't delete the last librarian account - promote another user to librarian first.");
        }
        passwordResetTokenRepository.deleteByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);
        readingProgressRepository.deleteByUser(user);
        userAchievementRepository.deleteByUser(user);
        myBookRepository.deleteByUser(user);
        reviewRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}