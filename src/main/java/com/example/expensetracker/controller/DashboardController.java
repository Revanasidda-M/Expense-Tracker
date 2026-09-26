package com.example.expensetracker.controller;

import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.model.Transaction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public DashboardController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, Object> dashboard(HttpServletRequest request) {
        Long userId = getUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        List<Transaction> month = transactionService.currentMonth(userId);
        BigDecimal totalIncome = sumByType(month, "INCOME");
        BigDecimal totalExpense = sumByType(month, "EXPENSE");
        BigDecimal balance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> categories = new LinkedHashMap<>();
        month.stream().filter(t -> t.getType().equals("EXPENSE"))
                .forEach(t -> categories.merge(t.getCategory(), t.getAmount(), BigDecimal::add));

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> chart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal income = amountForDate(month, date, "INCOME");
            BigDecimal expense = amountForDate(month, date, "EXPENSE");
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.toString());
            point.put("label", date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            point.put("income", income);
            point.put("expense", expense);
            chart.add(point);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("monthlyBudget", money(user.getMonthlyBudget()));
        result.put("totalIncome", money(totalIncome));
        result.put("totalExpense", money(totalExpense));
        result.put("remainingBalance", money(balance));
        result.put("totalTransactions", month.size());
        result.put("categoryTotals", categories);
        result.put("chart", chart);
        result.put("currentDate", today.toString());
        result.put("currentMonth", today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        return result;
    }

    private BigDecimal sumByType(List<Transaction> items, String type) {
        return items.stream().filter(t -> t.getType().equals(type))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal amountForDate(List<Transaction> items, LocalDate date, String type) {
        return items.stream()
                .filter(t -> t.getTransactionDate().equals(date) && t.getType().equals(type))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private Long getUserId(HttpServletRequest request) {
        Object value = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        if (value == null) throw new ApiException("Please log in first", HttpStatus.UNAUTHORIZED);
        return (Long) value;
    }
}
