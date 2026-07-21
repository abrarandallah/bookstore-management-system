package com.abrar.BOOKSTORE.controller;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;

import com.abrar.BOOKSTORE.service.MyBookListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {myBookListController.class})
@ExtendWith(SpringExtension.class)
class myBookListControllerTest {
    @Autowired
    private myBookListController myBookListController;

    @MockBean
    private MyBookListService myBookListService;

    /**
     * Method under test: {@link myBookListController#deleteMyList(int)}
     */
    @Test
    void testDeleteMyList() throws Exception {
        doNothing().when(myBookListService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteMyList/{id}", 1);
        MockMvcBuilders.standaloneSetup(myBookListController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/my_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
    }

    /**
     * Method under test: {@link myBookListController#deleteMyList(int)}
     */
    @Test
    void testDeleteMyList2() throws Exception {
        doNothing().when(myBookListService).deleteById(anyInt());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/deleteMyList/{id}", 1);
        requestBuilder.contentType("https://example.org/example");
        MockMvcBuilders.standaloneSetup(myBookListController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.model().size(0))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/my_books"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my_books"));
    }
}

