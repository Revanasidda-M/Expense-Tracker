package com.example.expensetracker.controller;

import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request,
                                                        HttpServletRequest httpRequest) {
        User user = authService.register(request);
        httpRequest.getSession(true).setAttribute("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful", "user", authService.userResponse(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                     HttpServletRequest httpRequest) {
        User user = authService.login(request);
        httpRequest.getSession(true).setAttribute("userId", user.getId());
        return ResponseEntity.ok(Map.of("message", "Login successful", "user", authService.userResponse(user)));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("message", "Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        Object userId = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }
        return ResponseEntity.ok(Map.of("loggedIn", true, "userId", userId));
    }
}
