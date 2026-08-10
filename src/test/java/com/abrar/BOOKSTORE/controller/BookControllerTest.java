package com.abrar.BOOKSTORE.controller;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.FileStorageService;
import com.abrar.BOOKSTORE.service.GenreService;
import com.abrar.BOOKSTORE.service.MyBookListService;
import com.abrar.BOOKSTORE.service.PagedResult;
import com.abrar.BOOKSTORE.service.RatingSummary;
import com.abrar.BOOKSTORE.service.ReviewService;
import com.abrar.BOOKSTORE.service.BookValidator;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

@ContextConfiguration(classes = { BookController.class, BookValidator.class })
@ExtendWith(SpringExtension.class)
class BookControllerTest {
        @Autowired
        private BookController bookController;

        @MockBean
        private BookService bookService;

        @MockBean
        private MyBookListService myBookListService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private FileStorageService fileStorageService;

        @MockBean
        private GenreService genreService;

        @MockBean
        private ReviewService reviewService;

        private static final Principal READER_PRINCIPAL = () -> "reader";

        private void stubCurrentUser() {
                User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
                when(userRepository.findByUsernameOrEmail("reader")).thenReturn(Optional.of(user));
        }

        /**
         * Method under test:
         * {@link BookController#addBook(Book, org.springframework.web.multipart.MultipartFile, String, java.util.List, Model)}
         */
        @Test
        void testAddBook() throws Exception {
                doNothing().when(bookService).save(Mockito.<Book>any());
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                                .param("name", "Name")
                                .param("author", "JaneDoe")
                                .param("takeaways[0].heading", "Heading")
                                .param("takeaways[0].content", "Content");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
        }

        /**
         * Method under test: {@link BookController#deleteMyBook(long, Principal)}
         */
        @Test
        void testDeleteMyBook() throws Exception {
                stubCurrentUser();
                doNothing().when(myBookListService).deleteById(anyLong(), Mockito.<User>any());
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/deleteMyBook/{id}", 1)
                                .principal(READER_PRINCIPAL);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.model().size(0))
                                .andExpect(MockMvcResultMatchers.view().name("redirect:/my_books"))
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
        }

        /**
         * Method under test: {@link BookController#deleteMyBook(long, Principal)}
         * Verifies the endpoint is POST-only - CSRF protection (and the
         * intentional avoidance of state-changing GET requests) only applies to
         * mutating HTTP methods.
         */
        @Test
        void testDeleteMyBookRejectsGet() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteMyBook/{id}", 1);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
        }

        /**
         * Method under test:
         * {@link BookController#getAllBook(String, String, Integer, int, Model)}
         */
        @Test
        void testGetAllBook() throws Exception {
                when(bookService.searchPaged(Mockito.<String>any(), Mockito.<Integer>any(), Mockito.<String>any(),
                                anyInt(), anyInt()))
                                .thenReturn(new PagedResult<>(new ArrayList<>(), 1, BookService.DEFAULT_PAGE_SIZE, 0));
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/available_books");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(7))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("pagination"))
                                .andExpect(MockMvcResultMatchers.view().name("bookList"))
                                .andExpect(MockMvcResultMatchers.forwardedUrl("bookList"));
        }

        /**
         * Method under test:
         * {@link BookController#getAllBookResultsFragment(String, String, Integer, int, Model)}
         */
        @Test
        void testGetAllBookResultsFragment() throws Exception {
                when(bookService.searchPaged(Mockito.<String>any(), Mockito.<Integer>any(), Mockito.<String>any(),
                                anyInt(), anyInt()))
                                .thenReturn(new PagedResult<>(new ArrayList<>(), 1, BookService.DEFAULT_PAGE_SIZE, 0));
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/available_books/results")
                                .param("q", "dune")
                                .param("sort", "author_asc");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(6))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("pagination"))
                                .andExpect(MockMvcResultMatchers.view().name("bookList :: resultsFragment"));
        }

        /**
         * Method under test: {@link BookController#randomBook()}
         */
        @Test
        void testRandomBook() throws Exception {
                Book book = new Book(7, "Name", "JaneDoe");
                when(bookService.getRandomBook()).thenReturn(book);
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/random_book");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books/7/read"));
        }

        /**
         * Method under test:
         * {@link BookController#addBook(Book, org.springframework.web.multipart.MultipartFile, String, java.util.List, Model)}
         */
        @Test
        void testAddBook2() throws Exception {
                doNothing().when(bookService).save(Mockito.<Book>any());
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                                .param("name", "Name")
                                .param("author", "JaneDoe")
                                .param("takeaways[0].heading", "Heading")
                                .param("takeaways[0].content", "Content")
                                .contentType("application/x-www-form-urlencoded");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
        }

        /**
         * Method under test:
         * {@link BookController#addBook(Book, org.springframework.web.multipart.MultipartFile, String, java.util.List, Model)}
         * Verifies that submitting a book with blank fields is rejected instead of
         * being saved.
         */
        @Test
        void testAddBookRejectsInvalidData() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                                .param("name", "")
                                .param("author", "");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                                .andExpect(MockMvcResultMatchers.view().name("bookRegister"));
        }

        /**
         * Method under test:
         * {@link BookController#addBook(Book, org.springframework.web.multipart.MultipartFile, String, java.util.List, Model)}
         * Otherwise-valid book with zero takeaways should be rejected - the
         * whole point of the format is that a book has at least one.
         */
        @Test
        void testAddBookRequiresAtLeastOneTakeaway() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                                .param("name", "Name")
                                .param("author", "JaneDoe");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                                .andExpect(MockMvcResultMatchers.view().name("bookRegister"));
                Mockito.verify(bookService, Mockito.never()).save(Mockito.<Book>any());
        }

        /**
         * Method under test: {@link BookController#shareBook(int, Model)}
         * The share page is public - no principal is set up for this
         * request, unlike {@link #testDeleteMyBook()} - and should still
         * resolve, with the first takeaway exposed as the teaser.
         */
        @Test
        void testShareBookWithTakeaways() throws Exception {
                Book book = new Book(1, "Atomic Habits", "James Clear");
                BookPage takeaway = new BookPage();
                takeaway.setHeading("Small habits compound");
                takeaway.setContent("1% better every day adds up.");
                book.setTakeaways(new ArrayList<>(List.of(takeaway)));
                when(bookService.getBookById(1)).thenReturn(book);
                when(reviewService.summaryForBook(1)).thenReturn(new RatingSummary(4.5, 2));

                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/available_books/{id}/share",
                                1);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.view().name("bookShare"))
                                .andExpect(MockMvcResultMatchers.model().attribute("book", book))
                                .andExpect(MockMvcResultMatchers.model().attribute("teaser", takeaway));
        }

        /**
         * Method under test: {@link BookController#shareBook(int, Model)}
         * A book with no takeaways yet should still render the share page,
         * just with a null teaser rather than an error.
         */
        @Test
        void testShareBookWithoutTakeaways() throws Exception {
                Book book = new Book(2, "Untitled Draft", "Jane Doe");
                when(bookService.getBookById(2)).thenReturn(book);
                when(reviewService.summaryForBook(2)).thenReturn(new RatingSummary(0.0, 0));

                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/available_books/{id}/share",
                                2);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.view().name("bookShare"))
                                .andExpect(MockMvcResultMatchers.model().attribute("teaser", (Object) null));
        }

        /**
         * Method under test: {@link BookController#BookRegister(Model)}
         */
        @Test
        void testBookRegister() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/book_register");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(1))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("allGenres"))
                                .andExpect(MockMvcResultMatchers.view().name("bookRegister"))
                                .andExpect(MockMvcResultMatchers.forwardedUrl("bookRegister"));
        }

        /**
         * Method under test: {@link BookController#deleteBook(int)}
         */
        @Test
        void testDeleteBook() throws Exception {
                doNothing().when(bookService).deleteById(anyInt());
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/deleteBook/{id}", 1);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.model().size(0))
                                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
        }

        /**
         * Method under test: {@link BookController#deleteBook(int)}
         * Verifies the endpoint is POST-only.
         */
        @Test
        void testDeleteBookRejectsGet() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteBook/{id}", 1);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
        }

        /**
         * Method under test: {@link BookController#editBook(int, Model)}
         */
        @Test
        void testEditBook() throws Exception {
                Book book = new Book();
                book.setAuthor("JaneDoe");
                book.setId(1);
                book.setName("Name");
                when(bookService.getBookById(anyInt())).thenReturn(book);
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/editBook/{id}", 1);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(3))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("allGenres"))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("selectedGenreIds"))
                                .andExpect(MockMvcResultMatchers.view().name("bookEdit"))
                                .andExpect(MockMvcResultMatchers.forwardedUrl("bookEdit"));
        }

        /**
         * Method under test: {@link BookController#getMyBooks(Model, Principal)}
         * Verifies only the current user's books come back, via the
         * user-scoped service call.
         */
        @Test
        void testGetMyBooks() throws Exception {
                stubCurrentUser();
                when(myBookListService.getMyBooks(Mockito.<User>any())).thenReturn(new ArrayList<>());
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/my_books")
                                .principal(READER_PRINCIPAL);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(1))
                                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                                .andExpect(MockMvcResultMatchers.view().name("myBooks"))
                                .andExpect(MockMvcResultMatchers.forwardedUrl("myBooks"));
        }

        /**
         * Method under test: {@link BookController#getMylist(int, Principal)}
         */
        @Test
        void testGetMyList() throws Exception {
                stubCurrentUser();
                when(myBookListService.alreadyInList(anyInt(), Mockito.<User>any())).thenReturn(false);
                doNothing().when(myBookListService).saveMyBooks(Mockito.<MyBookList>any());

                Book book = new Book();
                book.setAuthor("JaneDoe");
                book.setId(1);
                book.setName("Name");
                when(bookService.getBookById(anyInt())).thenReturn(book);
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/mylist/{id}", 1)
                                .principal(READER_PRINCIPAL);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.model().size(0))
                                .andExpect(MockMvcResultMatchers.view().name("redirect:/my_books"))
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
        }

        /**
         * Method under test: {@link BookController#getMylist(int, Principal)}
         * Adding a book already on the user's list shouldn't try to save a
         * second (now constraint-violating) row.
         */
        @Test
        void testGetMyListSkipsDuplicates() throws Exception {
                stubCurrentUser();
                when(myBookListService.alreadyInList(anyInt(), Mockito.<User>any())).thenReturn(true);

                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/mylist/{id}", 1)
                                .principal(READER_PRINCIPAL);
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isFound())
                                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
                Mockito.verify(myBookListService, Mockito.never()).saveMyBooks(Mockito.<MyBookList>any());
        }

        /**
         * Method under test: {@link BookController#home()}
         */
        @Test
        void testHome() throws Exception {
                MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/");
                MockMvcBuilders.standaloneSetup(bookController)
                                .build()
                                .perform(requestBuilder)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.model().size(0))
                                .andExpect(MockMvcResultMatchers.view().name("home"))
                                .andExpect(MockMvcResultMatchers.forwardedUrl("home"));
        }
}