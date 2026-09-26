package com.example.expensetracker.dto;

import com.example.expensetracker.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String type,
        String category,
        String description,
        LocalDate transactionDate
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getTransactionDate()
        );
    }
}
