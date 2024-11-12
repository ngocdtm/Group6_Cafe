package com.coffee.service;

import com.coffee.enums.OrderStatus;
import com.coffee.wrapper.BillWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BillService {

    ResponseEntity<List<BillWrapper>> getBills();

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> deleteBill(Integer id);

    ResponseEntity<Map<String,Object>> applyCoupon(Map<String, Object> requestMap);

    ResponseEntity<String> generateOfflineBill(Map<String, Object> requestMap);

    ResponseEntity<String> processOnlineOrder(Map<String, Object> requestMap);

    ResponseEntity<String> updateOrderStatus(Integer id, OrderStatus status);
}