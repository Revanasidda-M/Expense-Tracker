package com.example.expensetracker.controller;

import com.example.expensetracker.dto.TransactionRequest;
import com.example.expensetracker.dto.TransactionResponse;
import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) { this.transactionService = transactionService; }

    @GetMapping
    public List<TransactionResponse> getAll(HttpServletRequest request) {
        return transactionService.findAll(getUserId(request)).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request,
                                                      HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transactionService.create(getUserId(httpRequest), request)));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request,
                                      HttpServletRequest httpRequest) {
        return TransactionResponse.from(transactionService.update(getUserId(httpRequest), id, request));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id, HttpServletRequest request) {
        transactionService.delete(getUserId(request), id);
        return Map.of("message", "Transaction deleted successfully");
    }

    private Long getUserId(HttpServletRequest request) {
        Object value = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        if (value == null) throw new ApiException("Please log in first", HttpStatus.UNAUTHORIZED);
        return (Long) value;
    }
}
