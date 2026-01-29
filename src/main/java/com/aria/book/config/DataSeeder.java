package com.aria.book.config;

import com.aria.book.entity.Book;
import com.aria.book.entity.Member;
import com.aria.book.repository.BookRepository;
import com.aria.book.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(BookRepository bookRepository, MemberRepository memberRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
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

                bookRepository.saveAll(List.of(book1, book2));
                System.out.println("Sample books seeded into the database.");
            }
            if (memberRepository.count() == 0) {
                Member member1 = Member.builder()
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .build();

                Member member2 = Member.builder()
                        .name("Jane Smith")
                        .email("jane.smith@example.com")
                        .build();

                memberRepository.saveAll(List.of(member1, member2));
                System.out.println("Sample members seeded into the database.");
            }
        };
    }
}
