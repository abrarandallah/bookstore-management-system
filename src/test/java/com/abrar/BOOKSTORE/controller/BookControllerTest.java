package com.abrar.BOOKSTORE.controller;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.MyBookListService;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;

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

@ContextConfiguration(classes = { BookController.class })
@ExtendWith(SpringExtension.class)
class BookControllerTest {
    @Autowired
    private BookController bookController;

    @MockBean
    private BookService bookService;

    @MockBean
    private MyBookListService myBookListService;

    /**
     * Method under test:
     * {@link BookController#addBook(Book, org.springframework.validation.BindingResult, Model)}
     */
    @Test
    void testAddBook() throws Exception {
        doNothing().when(bookService).save(Mockito.<Book>any());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                .param("name", "Name")
                .param("author", "JaneDoe")
                .param("price", "19.99");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
    }

    /**
     * Method under test: {@link BookController#deleteMyBook(int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testDeleteMyBook() throws Exception {
        // TODO: Complete this test.
        // Reason: R013 No inputs found that don't throw a trivial exception.
        // Diffblue Cover tried to run the arrange/act section, but the method under
        // test threw
        // java.lang.IllegalStateException: Ambiguous mapping. Cannot map
        // 'com.abrar.BOOKSTORE.controller.BookController#5543cb55' method
        // com.abrar.BOOKSTORE.controller.BookController#deleteBook(int)
        // to {[DELETE, GET, POST, PUT] [/deleteBook/{id}]}: There is already
        // 'com.abrar.BOOKSTORE.controller.BookController#5543cb55' bean method
        // com.abrar.BOOKSTORE.controller.BookController#deleteMyBook(int) mapped.
        // at java.base/java.util.LinkedHashMap.forEach(LinkedHashMap.java:721)
        // See https://diff.blue/R013 to resolve this issue.

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteBook/{id}", 1);
        MockMvcBuilders.standaloneSetup(bookController).build().perform(requestBuilder);
    }

    /**
     * Method under test: {@link BookController#deleteMyBook(int)}
     */
    @Test
    void testDeleteMyBook2() throws Exception {
        doNothing().when(myBookListService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteMyBook/{id}", 1);
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
    }

    /**
     * Method under test: {@link BookController#deleteMyBook(int)}
     */
    @Test
    void testDeleteMyBook3() throws Exception {
        doNothing().when(myBookListService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteMyBook/{id}", 1);
        requestBuilder.contentType("https://example.org/example");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
    }

    /**
     * Method under test: {@link BookController#getAllBook()}
     */
    @Test
    void testGetAllBook() throws Exception {
        when(bookService.getAllBook()).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/available_books");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().size(1))
                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                .andExpect(MockMvcResultMatchers.view().name("bookList"))
                .andExpect(MockMvcResultMatchers.forwardedUrl("bookList"));
    }

    /**
     * Method under test:
     * {@link BookController#addBook(Book, org.springframework.validation.BindingResult, Model)}
     */
    @Test
    void testAddBook2() throws Exception {
        doNothing().when(bookService).save(Mockito.<Book>any());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                .param("name", "Name")
                .param("author", "JaneDoe")
                .param("price", "19.99")
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
     * {@link BookController#addBook(Book, org.springframework.validation.BindingResult, Model)}
     * Verifies that submitting a book with blank fields is rejected instead of
     * being saved.
     */
    @Test
    void testAddBookRejectsInvalidData() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/save")
                .param("name", "")
                .param("author", "")
                .param("price", "not-a-number");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                .andExpect(MockMvcResultMatchers.view().name("bookRegister"));
    }

    /**
     * Method under test: {@link BookController#BookRegister()}
     */
    @Test
    void testBookRegister() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/book_register");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("bookRegister"))
                .andExpect(MockMvcResultMatchers.forwardedUrl("bookRegister"));
    }

    /**
     * Method under test: {@link BookController#BookRegister()}
     */
    @Test
    void testBookRegister2() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/book_register", "Uri Variables");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("bookRegister"))
                .andExpect(MockMvcResultMatchers.forwardedUrl("bookRegister"));
    }

    /**
     * Method under test: {@link BookController#deleteBook(int)}
     */
    @Test
    void testDeleteBook() throws Exception {
        doNothing().when(bookService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteBook/{id}", 1);
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
     */
    @Test
    void testDeleteBook2() throws Exception {
        doNothing().when(bookService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteBook/{id}", 1);
        requestBuilder.contentType("https://example.org/example");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/available_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/available_books"));
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
        book.setPrice("Price");
        when(bookService.getBookById(anyInt())).thenReturn(book);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/editBook/{id}", 1);
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().size(1))
                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                .andExpect(MockMvcResultMatchers.view().name("bookEdit"))
                .andExpect(MockMvcResultMatchers.forwardedUrl("bookEdit"));
    }

    /**
     * Method under test: {@link BookController#getMyBooks(Model)}
     */
    @Test
    void testGetMyBooks() throws Exception {
        when(myBookListService.getAllMyBooks()).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/my_books");
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
     * Method under test: {@link BookController#getMylist(int)}
     */
    @Test
    void testGetMyList() throws Exception {
        doNothing().when(myBookListService).saveMyBooks(Mockito.<MyBookList>any());

        Book book = new Book();
        book.setAuthor("JaneDoe");
        book.setId(1);
        book.setName("Name");
        book.setPrice("Price");
        when(bookService.getBookById(anyInt())).thenReturn(book);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/mylist/{id}", 1);
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/my_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
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

    /**
     * Method under test: {@link BookController#home()}
     */
    @Test
    void testHome2() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/");
        requestBuilder.contentType("https://example.org/example");
        MockMvcBuilders.standaloneSetup(bookController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("home"))
                .andExpect(MockMvcResultMatchers.forwardedUrl("home"));
    }
}