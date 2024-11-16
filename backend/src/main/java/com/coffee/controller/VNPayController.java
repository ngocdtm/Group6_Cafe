package com.coffee.controller;

import com.coffee.entity.Payment;
import com.coffee.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
@Slf4j
public class VNPayController {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Payment paymentDTO, HttpServletRequest request) {
        log.info("Controller received payment request with shipping address: '{}'", paymentDTO.getShippingAddress());
        try {
            log.info("Received payment request: {}", paymentDTO);
            String paymentUrl = vnPayService.createPayment(paymentDTO, request);
            log.info("Generated payment URL: {}", paymentUrl);
            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("paymentUrl", paymentUrl);
                put("orderId", paymentDTO.getOrderId());
                put("status", "pending");
            }});
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                put("status", "failed");
                put("message", e.getMessage());
                put("orderId", paymentDTO.getOrderId());
            }});
        }
    }

    @GetMapping("/payment-callback")
    public void paymentCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String vnpayResponse = vnPayService.paymentCallback(request);
            String txnRef = request.getParameter("vnp_TxnRef");
            String transactionNo = request.getParameter("vnp_TransactionNo");

            StringBuilder redirectUrl = new StringBuilder(frontendUrl + "/payment-success");
            redirectUrl.append("?orderId=").append(txnRef);
            redirectUrl.append("&transactionNo=").append(transactionNo);

            if ("Payment Success".equals(vnpayResponse)) {
                redirectUrl.append("&status=success");
            } else {
                redirectUrl.append("&status=failed");
                redirectUrl.append("&message=").append(java.net.URLEncoder.encode(vnpayResponse, "UTF-8"));
            }

            log.info("Redirecting to: {}", redirectUrl.toString());
            response.sendRedirect(redirectUrl.toString());

        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage(), e);
            response.sendRedirect(frontendUrl + "/payment-failed?error=" +
                    java.net.URLEncoder.encode("Internal server error: " + e.getMessage(), "UTF-8"));
        }
    }
}