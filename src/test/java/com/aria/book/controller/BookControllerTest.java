package com.aria.book.controller;

import com.aria.book.entity.Book;
import com.aria.book.repository.BookRepository;
import com.aria.book.security.ApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private ApiKeyFilter apiKeyFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Book sampleBook;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        sampleBook = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .totalCopies(10)
                .availableCopies(10)
                .build();
    }

    @Test
    void listBooks_shouldReturnListOfBooks() throws Exception {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook));

        mockMvc.perform(get("/books/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"));

        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void createBook_shouldReturnCreatedBook() throws Exception {
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(post("/books/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_shouldReturnUpdatedBook() throws Exception {
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(put("/books/update/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()));

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldReturnOk() throws Exception {
        doNothing().when(bookRepository).deleteById(bookId);

        mockMvc.perform(delete("/books/delete/{id}", bookId))
                .andExpect(status().isOk());

        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    void updateBook_shouldFailWhenIdIsNull() throws Exception {
        Book bookWithoutId = Book.builder()
                .title("No ID Book")
                .author("Author")
                .build();

        mockMvc.perform(put("/books/update/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookWithoutId)))
                .andExpect(status().isInternalServerError());
    }
}
