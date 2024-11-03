package com.adminpanel.controller;

import com.adminpanel.model.Product;
import com.adminpanel.model.PurchaseLog;
import com.adminpanel.model.User;
import com.adminpanel.security.CustomAuthorizationService;
import com.adminpanel.security.JwtUtil;
import com.adminpanel.service.ProductService;
import com.adminpanel.service.PurchaseLogService;
import com.adminpanel.service.UserService;

import io.jsonwebtoken.MalformedJwtException;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private PurchaseLogService purchaseLogService;
    
    @Autowired
    private CustomAuthorizationService customAuthorizationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    
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
    public ResponseEntity<Product> addProduct(@RequestBody Product product, @RequestHeader("Authorization") String token) {
        Product createdProduct = productService.addProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Product>> getProductsByUserId(@RequestParam Long userId) {
        List<Product> products = productService.getProductsByUserId(userId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@RequestParam String id, @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct != null) {
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/buy")
    public ResponseEntity<String> buyProduct(@RequestParam String id, @RequestHeader("Authorization") String token, @RequestParam int quantity) {
    	try {
    		Long userId = getUserIdFromToken(token);
    		// Check if the product is available and user has enough balance
            productService.purchaseProduct(id, userId, quantity);
            
            return new ResponseEntity<>("Product purchased successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    //#################################
    
    @GetMapping("/admin/view/{productId}")
    public ResponseEntity<?> getProductDetailsWithPurchases(@RequestParam String productId, Authentication authentication) {
        String role = authentication.getAuthorities().stream().findFirst().get().getAuthority();
        
        if (!role.equals("admin")) {
            return new ResponseEntity<>("Unauthorized access. Admin only.", HttpStatus.UNAUTHORIZED);
        }

        // Fetch product details
        Product product = productService.getProductById(productId);
        if (product == null) {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }

        // Fetch purchase logs for the product
        List<PurchaseLog> purchaseLogs = purchaseLogService.getPurchaseLogsByProductId(productId);

        // Combine product details and purchase logs
        ProductWithPurchasesResponse response = new ProductWithPurchasesResponse(product, purchaseLogs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // DTO for the response
    public static class ProductWithPurchasesResponse {
        private Product product;
        private List<PurchaseLog> purchaseLogs;

        public ProductWithPurchasesResponse(Product product, List<PurchaseLog> purchaseLogs) {
            this.product = product;
            this.purchaseLogs = purchaseLogs;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public List<PurchaseLog> getPurchaseLogs() {
            return purchaseLogs;
        }

        public void setPurchaseLogs(List<PurchaseLog> purchaseLogs) {
            this.purchaseLogs = purchaseLogs;
        }
    }

}
