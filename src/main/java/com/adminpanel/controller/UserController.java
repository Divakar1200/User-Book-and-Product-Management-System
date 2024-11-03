package com.adminpanel.controller;

import java.util.Optional;

import com.adminpanel.repository.*;
import com.adminpanel.dto.SignupResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.adminpanel.model.User;
import org.springframework.security.core.Authentication;
import com.adminpanel.security.CustomAuthorizationService;
import com.adminpanel.security.JwtUtil;
import com.adminpanel.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.MalformedJwtException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private CustomAuthorizationService customAuthorizationService;
	
	@Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    private Long getUserIdFromToken(String token) {
    	try {
	        if (token != null && token.startsWith("Bearer ")) {
	            token = token.substring(7); // Remove "Bearer " prefix
	        }
	        String username = jwtUtil.extractUsername(token);
	        User user = userService.findByUsername(username);
	        return user.getId();
    	} catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
    
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) throws JsonProcessingException {
    	ObjectMapper map = new ObjectMapper();   //this object is used to print user details in console
    	System.out.println("user :"+ map.writeValueAsString(user) ); 
    	
    	// Save the user (JWT token will be added to the user before saving)
        String token = jwtUtil.generateToken(user.getUsername(),user.getRole());
        user.setJwtToken(token); // Set the JWT token in the user object
        
        User savedUser = userService.save(user); // Save the user

        System.out.println("token :"+token);
     // Create the SignupResponse object
        SignupResponse response = new SignupResponse(token, user.getUsername(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
    	try {
    		// Check if the user exists by username
	        User existingUser = userService.findByUsername(user.getUsername());
	        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
	            String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());
	            
	         // Save the token to the user's record in the database
	            existingUser.setJwtToken(token);
	            userService.update(existingUser);  // Update user with the token
	            
	            return ResponseEntity.ok("Token : " + token + "\nSuccessfully loged in");
	        } else {
	            return ResponseEntity.status(401).body("Unauthorized");
	        	
	        }
    	} catch (Exception ex) {
    		System.out.println("Login :"+ ex.getMessage());
    		return ResponseEntity.status(500).body("Something went wrong!");
		}
    }
    
    @GetMapping("/getuserbyID")
    public ResponseEntity<?> getUserById(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        try {
        	// Extract the JWT token and validate the role
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
               
            }

            // Check if the token is valid for admin role
            Boolean isValid = jwtUtil.validateToken(token, "admin");
            System.out.println(isValid);
            if (!isValid) {
//            	System.out.println(isValid);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can find user by ID");
            }
        	
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get()); // Return the user object, not the Optional
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            System.err.println("Error fetching user: " + ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestParam long id, @RequestBody User userDetails, @RequestHeader("Authorization") String token) {
    	try {
    		// Extract the JWT token and validate the role
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }

            // Check if the token is valid for admin role
            Boolean isValid = jwtUtil.validateToken(token, "admin");
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can update user");
            }
    		
	        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
	        
	        // Update username, email and role
	        user.setUsername(userDetails.getUsername());
	        user.setEmail(userDetails.getEmail());
	        user.setRole(userDetails.getRole());
	        
	     // Update password securely
	        String newPassword = userDetails.getPassword();
	        if (newPassword != null && !newPassword.isEmpty()) {
	            user.setPassword(passwordEncoder.encode(newPassword)); // Encrypt the new password
	        }
	
	        // Generate a new JWT token based on the updated username
	        String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
	        user.setJwtToken(newToken); // Update the JWT token in the user object
	
	        // Save the updated user object
	        User updatedUser = userService.update(user);
	        
	        return ResponseEntity.ok(updatedUser);
    	} catch (RuntimeException ex) {
    	        // Handle the case when the ID is not found
    	        System.err.println("Error updating user: " + ex.getMessage());
    	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID not found");
    	    } catch (Exception ex) {
    	        // Handle any other exceptions
    	        System.err.println("Error updating user: " + ex.getMessage());
    	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
    	}
    }

    
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        try {
        	// Extract the JWT token and validate the role
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }

            // Check if the token is valid for admin role
            Boolean isValid = jwtUtil.validateToken(token, "admin");
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can delete user by ID");
            }
        	
            // Check if the user exists
            User user = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

            // If user exists, delete the user
            userService.deleteById(id);
            
            return ResponseEntity.ok("User deleted");
        } catch (RuntimeException ex) {
            // Handle the case when the ID is not found
            System.err.println("Error deleting user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception ex) {
            // Handle any other exceptions
            System.err.println("Error deleting user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }
    
    
//##########################################
    
    // Add balance to user account
    @PostMapping("/addBalance")
    public ResponseEntity<String> addBalance(@RequestParam("amount") double amount, @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        userService.addBalance(userId, amount);
        return ResponseEntity.ok("Balance added successfully!");
    }

    // Check user's balance
    @GetMapping("/checkBalance")
    public ResponseEntity<Double> checkBalance(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        double balance = userService.checkBalance(userId);
        return ResponseEntity.ok(balance);
    }
    
}
