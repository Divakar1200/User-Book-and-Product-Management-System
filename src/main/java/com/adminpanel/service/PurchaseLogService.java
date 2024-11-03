package com.adminpanel.service;

import com.adminpanel.model.PurchaseLog;
import com.adminpanel.repository.PurchaseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseLogService {

    @Autowired
    private PurchaseLogRepository purchaseLogRepository;

    public List<PurchaseLog> getPurchaseLogsByProductId(String productId) {
        return purchaseLogRepository.findByProductId(productId);
    }
}

