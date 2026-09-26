package com.example.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RegisterRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @NotBlank @Email @Size(max = 150)
    private String email;
    @NotBlank @Size(min = 6, max = 100)
    private String password;
    @DecimalMin(value = "0.0")
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
}
