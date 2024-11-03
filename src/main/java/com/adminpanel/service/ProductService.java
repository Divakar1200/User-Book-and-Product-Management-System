package com.adminpanel.service;

import com.adminpanel.model.Product;
import com.adminpanel.model.PurchaseLog;
import com.adminpanel.model.User;
import com.adminpanel.repository.ProductRepository;
import com.adminpanel.repository.PurchaseLogRepository;
import com.adminpanel.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PurchaseLogRepository purchaseLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByUserId(Long userId) {
        return productRepository.findByUserId(userId);
    }

    public Product updateProduct(String id, Product product) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setQuantity(product.getQuantity());
            return productRepository.save(existingProduct);
        }
        return null;  // Or throw an exception
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
    
    public Product getProductById(String id) {
        return productRepository.findById(id).orElse(null);  // Fetch product by ID
    }
    
    
    @Transactional
    public void purchaseProduct(String productId, Long userId, int quantity) {
        // Fetch product details
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient product quantity or product not found.");
        }

        // Calculate total price
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));


        // Fetch user details
        User buyer = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the user has enough balance
        if (BigDecimal.valueOf(buyer.getBalance()).compareTo(totalPrice) < 0) {
            throw new RuntimeException("Insufficient balance to purchase the product.");
        }
        
        Long ownerId = product.getUserId(); // Assuming 'userId' represents the owner
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Product owner not found"));

        // Update product quantity
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        // Deduct balance from buyer
        buyer.setBalance(BigDecimal.valueOf(buyer.getBalance()).subtract(totalPrice).doubleValue());
        userRepository.save(buyer);
      
        // Add balance to product owner
        owner.setBalance(BigDecimal.valueOf(owner.getBalance()).add(totalPrice).doubleValue());
        userRepository.save(owner);

        // Log the purchase
        PurchaseLog purchaseLog = new PurchaseLog(productId, userId, quantity, new Date());
        purchaseLogRepository.save(purchaseLog);
    }

    
}

