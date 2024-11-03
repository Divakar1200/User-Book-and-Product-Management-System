package com.adminpanel.dto;

import java.math.BigDecimal;
import java.util.Date;

public class ProductResponse {
    
    private String id;                     // Product ID
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private Long userId;                 // User ID who owns the product
    private Date createdAt;

    // Default Constructor
    public ProductResponse() {
    }

    // Parameterized Constructor
    public ProductResponse(String id, String name, String description, BigDecimal price, int quantity, Long userId, Date createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

