package com.aria.book.controller;

import com.aria.book.entity.Book;
import com.aria.book.entity.Borrow;
import com.aria.book.entity.Loan;
import com.aria.book.repository.BookRepository;
import com.aria.book.repository.LoanRepository;
import com.aria.book.security.ApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "MAX_LOAN=3",
    "MAX_DAYS=14"
})
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanRepository loanRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private ApiKeyFilter apiKeyFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Loan sampleLoan;
    private Book sampleBook;
    private UUID loanId;
    private UUID bookId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        sampleBook = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .totalCopies(5)
                .availableCopies(5)
                .build();

        sampleLoan = Loan.builder()
                .id(loanId)
                .bookId(bookId)
                .memberId(memberId)
                .borrowedAt(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .build();
    }

    @Test
    void listLoans_shouldReturnListOfLoans() throws Exception {
        when(loanRepository.findAll()).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/loan/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(bookId.toString()));

        verify(loanRepository, times(1)).findAll();
    }

    @Test
    void borrowBook_shouldReturnCreatedLoan() throws Exception {
        Borrow borrow = new Borrow(bookId, memberId);

        when(loanRepository.findByMemberId(memberId)).thenReturn(Arrays.asList());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook));
        when(loanRepository.save(any(Loan.class))).thenReturn(sampleLoan);

        mockMvc.perform(post("/loan/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()));

        verify(bookRepository, times(1)).save(any(Book.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void borrowBook_shouldFailWhenMaxLoanReached() throws Exception {
        Borrow borrow = new Borrow(bookId, memberId);
        Loan loan1 = new Loan();
        Loan loan2 = new Loan();
        Loan loan3 = new Loan();

        when(loanRepository.findByMemberId(memberId)).thenReturn(Arrays.asList(loan1, loan2, loan3));

        mockMvc.perform(post("/loan/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void borrowBook_shouldFailWhenOverdueLoan() throws Exception {
        Borrow borrow = new Borrow(bookId, memberId);
        Loan overdueLoan = Loan.builder()
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(loanRepository.findByMemberId(memberId)).thenReturn(Arrays.asList(overdueLoan));

        mockMvc.perform(post("/loan/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnBook_shouldSetReturnedAt() throws Exception {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(sampleLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(sampleLoan);

        mockMvc.perform(put("/loan/return/{id}", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedAt").exists());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void getLoanById_shouldReturnLoan() throws Exception {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(sampleLoan));

        mockMvc.perform(get("/loan/{id}", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId.toString()));
    }
}
