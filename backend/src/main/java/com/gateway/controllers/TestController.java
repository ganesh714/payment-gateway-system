package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.services.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/merchant")
    public ResponseEntity<Map<String, Object>> getTestMerchant() {
        Merchant merchant = merchantService.getTestMerchant();
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", merchant.getId());
        response.put("email", merchant.getEmail());
        response.put("name", merchant.getName());
        response.put("apiKey", merchant.getApiKey());
        response.put("apiSecret", merchant.getApiSecret());
        response.put("seeded", true);

        return ResponseEntity.ok(response);
    }
}
