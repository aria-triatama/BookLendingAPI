package com.aria.book.config;

import com.aria.book.entity.Book;
import com.aria.book.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(BookRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Book book1 = Book.builder()
                        .title("The Clean Coder")
                        .author("Robert C. Martin")
                        .isbn("978-0137081073")
                        .totalCopies(10)
                        .availableCopies(10)
                        .build();

                Book book2 = Book.builder()
                        .title("Design Patterns")
                        .author("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                        .isbn("978-0201633610")
                        .totalCopies(5)
                        .availableCopies(5)
                        .build();

                repository.saveAll(List.of(book1, book2));
                System.out.println("Sample books seeded into the database.");
            }
        };
    }
}
