package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ProfileRequest;
import com.example.expensetracker.exception.ApiException;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping
    public Map<String, Object> get(HttpServletRequest request) {
        User user = getUser(request);
        return toMap(user);
    }

    @PutMapping
    public Map<String, Object> update(@Valid @RequestBody ProfileRequest profileRequest,
                                      HttpServletRequest request) {
        User user = getUser(request);
        user.setName(profileRequest.getName().trim());
        user.setMonthlyBudget(profileRequest.getMonthlyBudget());
        return toMap(userRepository.save(user));
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("monthlyBudget", user.getMonthlyBudget());
        return map;
    }

    private User getUser(HttpServletRequest request) {
        Object id = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        if (id == null) throw new ApiException("Please log in first", HttpStatus.UNAUTHORIZED);
        return userRepository.findById((Long) id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
