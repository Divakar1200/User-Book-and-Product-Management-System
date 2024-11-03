package com.adminpanel.repository;

import com.adminpanel.model.PurchaseLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PurchaseLogRepository extends MongoRepository<PurchaseLog, String> {
    List<PurchaseLog> findByUserId(Long userId);
    
    List<PurchaseLog> findByProductId(String productId);
}
