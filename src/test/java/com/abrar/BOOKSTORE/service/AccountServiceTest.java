package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.PasswordResetTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.repository.MyBookRepository;
import com.abrar.BOOKSTORE.repository.ReviewRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { AccountService.class })
@ExtendWith(SpringExtension.class)
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MyBookRepository myBookRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void testDeleteAccountRemovesEverythingScopedToTheUser() {
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        doNothing().when(passwordResetTokenRepository).deleteByUser(user);
        doNothing().when(myBookRepository).deleteByUser(user);
        doNothing().when(reviewRepository).deleteByUser(user);
        doNothing().when(userRepository).delete(user);

        accountService.deleteAccount(user);

        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(myBookRepository).deleteByUser(user);
        verify(reviewRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteAccountDeletesDependentDataBeforeTheUserRow() {
        // Order matters here: the User row can't be deleted while a
        // password-reset token or review still holds a foreign key to it.
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        doNothing().when(passwordResetTokenRepository).deleteByUser(user);
        doNothing().when(myBookRepository).deleteByUser(user);
        doNothing().when(reviewRepository).deleteByUser(user);
        doNothing().when(userRepository).delete(user);

        accountService.deleteAccount(user);

        InOrder inOrder = Mockito.inOrder(passwordResetTokenRepository, myBookRepository, reviewRepository,
                userRepository);
        inOrder.verify(passwordResetTokenRepository).deleteByUser(user);
        inOrder.verify(myBookRepository).deleteByUser(user);
        inOrder.verify(reviewRepository).deleteByUser(user);
        inOrder.verify(userRepository).delete(user);
    }

    @Test
    void testDeleteAccountAllowsALibrarianWhenOthersRemain() {
        User librarian = new User("librarian", "librarian@example.com", "hash", "ROLE_LIBRARIAN");
        when(userRepository.countByRole("ROLE_LIBRARIAN")).thenReturn(2L);

        accountService.deleteAccount(librarian);

        verify(userRepository).delete(librarian);
    }

    @Test
    void testDeleteAccountRejectsTheLastLibrarian() {
        User lastLibrarian = new User("librarian", "librarian@example.com", "hash", "ROLE_LIBRARIAN");
        when(userRepository.countByRole("ROLE_LIBRARIAN")).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> accountService.deleteAccount(lastLibrarian));

        verify(userRepository, never()).delete(Mockito.<User>any());
        verify(myBookRepository, never()).deleteByUser(Mockito.<User>any());
    }
}