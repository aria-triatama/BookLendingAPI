package com.aria.book.controller;

import com.aria.book.entity.Loan;
import com.aria.book.entity.Book;
import com.aria.book.entity.Borrow;
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
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/loan")
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
        return loanRepository.findAll();
    }

    @PostMapping("/borrow")
    public Loan borrowBook(@RequestBody Borrow borrow) {
        List<Loan> existingLoan = loanRepository.findByMemberId(borrow.getMemberId()).stream().filter(loan -> loan.getReturnedAt() == null).toList();
        if (existingLoan.size() >= maxLoan) {
            throw new RuntimeException("Member has reached the maximum number of loans");
        }
        for (Loan loan : existingLoan) {
            if (loan.getDueDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Member has overdue loans");
            }
        }
        Loan loan = new Loan();
        loan.setBookId(borrow.getBookId());
        loan.setMemberId(borrow.getMemberId());
        loan.setBorrowedAt(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(maxDays));
        Book existingBook = bookRepository.findById(borrow.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        existingBook.setAvailableCopies(existingBook.getAvailableCopies() - 1);
        bookRepository.save(existingBook);
        return loanRepository.save(loan);
    }

    @PutMapping("/return/{id}")
    public Loan returnBook(@PathVariable UUID id) {
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        existingLoan.setReturnedAt(LocalDate.now());
        return loanRepository.save(existingLoan);
    }

    @GetMapping("/{id}")
    public Loan getLoanById(@PathVariable UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }
}
