package com.coffee.service;



import com.coffee.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface VNPayService {
    String createPayment(Payment paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException;
    String paymentCallback(HttpServletRequest request);
}
