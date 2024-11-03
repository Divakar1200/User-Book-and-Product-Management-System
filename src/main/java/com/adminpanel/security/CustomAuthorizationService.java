package com.adminpanel.security;

import org.springframework.stereotype.Service;

@Service
public class CustomAuthorizationService {
	public boolean isAuthorized(Long userId, String username) {
        // Implement your custom logic here
        // For example, check if the userId belongs to the logged-in user
        return userId.equals(getUserIdByUsername(username));
    }

    private Long getUserIdByUsername(String username) {
        // Replace with logic to fetch user ID based on username
        // For example, query your database
        return 1L; // Placeholder
    }
}