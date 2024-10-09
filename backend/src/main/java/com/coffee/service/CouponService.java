package com.coffee.service;

import com.coffee.wrapper.CouponWrapper;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CouponService {
    ResponseEntity<String> addCoupon(Map<String, String> requestMap);

    ResponseEntity<List<CouponWrapper>> getAllCoupon();

    ResponseEntity<String> updateCoupon(Map<String, String> requestMap);

    ResponseEntity<String> deleteCoupon(Long id);

    ResponseEntity<CouponWrapper> getCouponById(Long id);
}
