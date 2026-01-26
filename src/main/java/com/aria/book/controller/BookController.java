package com.aria.book.controller;

import com.aria.book.entity.Book;
import com.aria.book.repository.BookRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/list")
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    @PostMapping("/create")
    public Book createBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    @PutMapping("/update")
    public Book updateBook(@RequestBody Book book) {
        if (book.getId() == null) {
            throw new IllegalArgumentException("Book ID must not be null for update");
        }
        return bookRepository.save(book);
    }

    @DeleteMapping("/delete")
    public void deleteBook(@RequestParam UUID id) {
        bookRepository.deleteById(id);
    }
}
