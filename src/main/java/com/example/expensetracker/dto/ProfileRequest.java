package com.example.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProfileRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @DecimalMin(value = "0.0")
    private BigDecimal monthlyBudget;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
}
