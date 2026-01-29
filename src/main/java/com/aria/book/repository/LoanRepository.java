package com.aria.book.repository;

import com.aria.book.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByMemberId(UUID memberId);
}