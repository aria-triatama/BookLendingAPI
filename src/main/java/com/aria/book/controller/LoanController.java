package com.aria.book.controller;

import com.aria.book.entity.Loan;
import com.aria.book.entity.Book;
import com.aria.book.entity.Borrow;
import com.aria.book.exception.BusinessException;
import com.aria.book.exception.ResourceNotFoundException;
import com.aria.book.repository.LoanRepository;
import com.aria.book.repository.BookRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/loan")
@Slf4j
public class LoanController {
    @Value("${MAX_LOAN}")
    private Integer maxLoan;
    @Value("${MAX_DAYS}")
    private Integer maxDays;    
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    public LoanController(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/list")
    public List<Loan> listLoans() {
        log.info("Fetching all loans");
        return loanRepository.findAll();
    }

    @PostMapping("/borrow")
    public Loan borrowBook(@RequestBody Borrow borrow) {
        log.info("Request to borrow book: {} by member: {}", borrow.getBookId(), borrow.getMemberId());
        List<Loan> existingLoan = loanRepository.findByMemberId(borrow.getMemberId()).stream().filter(loan -> loan.getReturnedAt() == null).toList();
        if (existingLoan.size() >= maxLoan) {
            log.warn("Member {} reached maximum loan limit", borrow.getMemberId());
            throw new BusinessException("Member has reached the maximum number of loans");
        }
        for (Loan loan : existingLoan) {
            if (loan.getDueDate().isBefore(LocalDate.now())) {
                log.warn("Member {} has overdue loan: {}", borrow.getMemberId(), loan.getId());
                throw new BusinessException("Member has overdue loans");
            }
        }
        Loan loan = new Loan();
        loan.setBookId(borrow.getBookId());
        loan.setMemberId(borrow.getMemberId());
        loan.setBorrowedAt(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(maxDays));
        Book existingBook = bookRepository.findById(borrow.getBookId())
                .orElseThrow(() -> {
                    log.error("Book not found: {}", borrow.getBookId());
                    return new ResourceNotFoundException("Book not found");
                });
        existingBook.setAvailableCopies(existingBook.getAvailableCopies() - 1);
        bookRepository.save(existingBook);
        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan created successfully: {}", savedLoan.getId());
        return savedLoan;
    }

    @PutMapping("/return/{id}")
    public Loan returnBook(@PathVariable UUID id) {
        log.info("Request to return book for loan: {}", id);
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Loan not found for return: {}", id);
                    return new ResourceNotFoundException("Loan not found");
                });
        existingLoan.setReturnedAt(LocalDate.now());
        log.info("Book returned successfully for loan: {}", id);
        Book existingBook = bookRepository.findById(existingLoan.getBookId())
                .orElseThrow(() -> {
                    log.error("Book not found: {}", existingLoan.getBookId());
                    return new ResourceNotFoundException("Book not found");
                });
        existingBook.setAvailableCopies(existingBook.getAvailableCopies() + 1);
        bookRepository.save(existingBook);
        return loanRepository.save(existingLoan);
    }

    @GetMapping("/{id}")
    public Loan getLoanById(@PathVariable UUID id) {
        log.info("Fetching loan details: {}", id);
        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Loan not found: {}", id);
                    return new ResourceNotFoundException("Loan not found");
                });
    }
}
