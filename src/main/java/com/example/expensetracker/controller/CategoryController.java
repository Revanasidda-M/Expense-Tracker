package com.example.expensetracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @GetMapping
    public List<String> categories() {
        return List.of("Food", "Transport", "Shopping", "Bills", "Education", "Health", "Entertainment", "Other");
    }
}
