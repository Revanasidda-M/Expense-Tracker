package com.example.expensetracker.service;

import com.example.expensetracker.dto.TransactionRequest;
import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.model.Transaction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.TransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> findAll(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId);
    }

    public Transaction create(Long userId, TransactionRequest request) {
        User user = getUser(userId);
        Transaction transaction = new Transaction();
        copy(request, transaction);
        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }

    public Transaction update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = getOwned(userId, id);
        copy(request, transaction);
        return transactionRepository.save(transaction);
    }

    public void delete(Long userId, Long id) {
        transactionRepository.delete(getOwned(userId, id));
    }

    public List<Transaction> currentMonth(Long userId) {
        LocalDate today = LocalDate.now();
        return transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
    }

    private void copy(TransactionRequest request, Transaction transaction) {
        String type = request.getType().trim().toUpperCase();
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            throw new ApiException("Transaction type must be INCOME or EXPENSE", HttpStatus.BAD_REQUEST);
        }
        transaction.setAmount(request.getAmount());
        transaction.setType(type);
        transaction.setCategory(request.getCategory().trim());
        transaction.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
        transaction.setTransactionDate(request.getTransactionDate());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private Transaction getOwned(Long userId, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ApiException("Transaction not found", HttpStatus.NOT_FOUND));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ApiException("You cannot access this transaction", HttpStatus.FORBIDDEN);
        }
        return transaction;
    }
}
