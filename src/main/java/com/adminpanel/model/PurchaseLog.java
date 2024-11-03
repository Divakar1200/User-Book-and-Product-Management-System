package com.adminpanel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "purchase_logs")
public class PurchaseLog {
    @Id
    private String id;
    private String productId;
    private Long userId; // The ID of the customer
    private int quantity;
    private Date purchaseDate;

    public PurchaseLog(String productId, Long userId, int quantity, Date purchaseDate) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

}
