package com.example.expensetracker.repository;

import com.example.expensetracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate);
}
