package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ApiException("Email is already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setMonthlyBudget(request.getMonthlyBudget() == null ? BigDecimal.ZERO : request.getMonthlyBudget());
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    public Map<String, Object> userResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("monthlyBudget", user.getMonthlyBudget());
        return response;
    }
}
