package com.adminpanel.controller;

import com.adminpanel.model.Book;
import com.adminpanel.model.User;
import com.adminpanel.security.JwtUtil;
import com.adminpanel.service.BookService;
import com.adminpanel.service.UserService;

import io.jsonwebtoken.MalformedJwtException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/books")
public class BookController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    

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
    
    @PostMapping("/add")
    public ResponseEntity<?> addBook(@RequestBody Book book, @RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
        	
            Boolean isValid = jwtUtil.validateToken(token, "user");
            System.out.println(isValid);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user can add book");
            }
        	
            Long userId = getUserIdFromToken(token);
            System.out.println("UserToken : "+token);
            System.out.println("userId : "+ userId);
            
            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            book.setUser(user);
            Book savedBook = bookService.save(book);
            
            return ResponseEntity.ok(savedBook + "Book saved successfully");
        } catch (Exception ex) {
        	System.err.println("Error while adding book: " + ex.getMessage());
        	ex.printStackTrace(); // This will print the stack trace to the console for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the book."+ ex.getMessage());
        }
    }
    
    
    @PutMapping("/update")
    public ResponseEntity<?> updateBook(@RequestParam Long id, @RequestBody Book book, @RequestHeader("Authorization") String token) {
        try {
        	if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
        	
            Boolean isValid = jwtUtil.validateToken(token, "user");
            System.out.println(isValid);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user can add book");
            }
            
            Long userId = getUserIdFromToken(token);
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            Optional<Book> existingBookOpt = bookService.findById(id);
            if (existingBookOpt.isPresent()) {
                Book existingBook = existingBookOpt.get();
                existingBook.setName(book.getName());
                existingBook.setDescription(book.getDescription());
                existingBook.setAuthor(book.getAuthor());
                existingBook.setPrice(book.getPrice());
                existingBook.setUser(user); // Assuming the user can be updated

                Book updatedBook = bookService.update(existingBook);
                return ResponseEntity.ok(updatedBook + " Book updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating the book." + ex.getMessage());
        }
    }

    
    
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteBook(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        try {
        	if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
        	
            Boolean isValid = jwtUtil.validateToken(token, "user");
            System.out.println(isValid);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user can add book");
            }
            
            Long userId = getUserIdFromToken(token);
            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            Optional<Book> existingBookOpt = bookService.findById(id);
            if (existingBookOpt.isPresent()) {
                bookService.deleteById(id);
                return ResponseEntity.ok("Book deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting the book." + ex.getMessage());
        }
    }
    
    @PostMapping("/userbooks")
    public ResponseEntity<?> getUserBooks(@RequestBody User userRequest) {
        try {
            User existingUser = userService.findByUsername(userRequest.getUsername());
	        if (existingUser != null && passwordEncoder.matches(userRequest.getPassword(), existingUser.getPassword())) {
	        	List<Book> userBooks = bookService.findByUserId(existingUser.getId());
	        	System.out.println(existingUser.getId() + " : user Id receving from findByUserID");
	        	return ResponseEntity.ok(userBooks);
	        }
	        else {
	        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credential");
	        }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching the books: " + ex.getMessage());
        }
    }
    
}