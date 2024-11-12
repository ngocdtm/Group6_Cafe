package com.coffee.controller;


import com.coffee.entity.Payment;
import com.coffee.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
@Slf4j
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Payment paymentDTO, HttpServletRequest request) {
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
    public ResponseEntity<?> paymentCallback(HttpServletRequest request) {
        try {
            String vnpayResponse = vnPayService.paymentCallback(request);
            String txnRef = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String transactionNo = request.getParameter("vnp_TransactionNo");

            Map<String, String> response = new HashMap<>();
            response.put("orderId", txnRef);
            response.put("transactionNo", transactionNo);

            if ("Payment Success".equals(vnpayResponse)) {
                response.put("status", "success");
                response.put("message", "Payment completed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "failed");
                response.put("message", vnpayResponse); // Now includes detailed error message
                response.put("responseCode", responseCode);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Internal server error: " + e.getMessage(),
                    "orderId", request.getParameter("vnp_TxnRef")
            ));
        }
    }
}