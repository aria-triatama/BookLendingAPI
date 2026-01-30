package com.aria.book.controller;

import com.aria.book.entity.Book;
import com.aria.book.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/list")
    public List<Book> listBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll();
    }

    @PostMapping("/create")
    public Book createBook(@RequestBody Book book) {
        log.info("Creating new book: {}", book.getTitle());
        return bookRepository.save(book);
    }

    @PutMapping("/update/{id}")
    public Book updateBook(@PathVariable UUID id, @RequestBody Book book) {
        log.info("Updating book: {}", id);
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found for update: {}", id);
                    return new ResourceNotFoundException("Book not found");
                });
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setTotalCopies(book.getTotalCopies());
        existingBook.setAvailableCopies(book.getAvailableCopies());
        return bookRepository.save(existingBook);
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable UUID id) {
        log.info("Fetching book details: {}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found: {}", id);
                    return new ResourceNotFoundException("Book not found");
                });
    }


    @DeleteMapping("/delete/{id}")
    public void deleteBook(@PathVariable UUID id) {
        log.info("Deleting book: {}", id);
        bookRepository.deleteById(id);
    }
}
