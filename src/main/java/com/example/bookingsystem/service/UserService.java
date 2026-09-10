package com.example.bookingsystem.service;

import com.example.bookingsystem.entity.User;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.repository.UserRepository;
import com.example.bookingsystem.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return principal.getUser();
    }

    public boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }
}
