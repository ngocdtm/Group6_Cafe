package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Coupon;
import com.coffee.repository.CouponRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.CouponService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.CouponWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Override
    public ResponseEntity<String> addCoupon(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateCouponMap(requestMap, false)){
                    Coupon coupon = couponRepository.findByNameCoupon(requestMap.get(CafeConstants.COUPON));
                    if (Objects.isNull(coupon)) {
                        couponRepository.save(getCouponFromMap(requestMap, false));
                        return CafeUtils.getResponseEntity("Coupon Added successfully", HttpStatus.OK);
                    }
                    else {
                        return CafeUtils.getResponseEntity(CafeConstants.NAMECOUPON_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCouponMap(Map<String, String> requestMap, boolean validateId) {
        return requestMap.containsKey("name") && (requestMap.containsKey("id") || !validateId);
    }

    private Coupon getCouponFromMap(Map<String, String> requestMap, boolean isAdd) throws ParseException {
        Coupon coupon = new Coupon();
        if(isAdd) {
            coupon.setId(Long.valueOf(requestMap.get("id")));
        }
        coupon.setName(requestMap.get("name"));
        coupon.setCode(requestMap.get("code"));
        coupon.setDiscount(Long.parseLong(requestMap.get("discount")));
        // Parse the date
        String expirationDateString = requestMap.get("expirationDate");
        if (expirationDateString != null && !expirationDateString.isEmpty()) { try
        { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expirationDate = LocalDate.parse(expirationDateString, formatter); coupon.setExpirationDate(expirationDate); }
        catch (DateTimeParseException e) {
            e.printStackTrace(); // Handle the parse exception, maybe set a default date or return null } }
        }}

            return coupon;
    }

    @Override
    public ResponseEntity<List<CouponWrapper>> getAllCoupon() {
        try{
            return new ResponseEntity<>(couponRepository.getAllCoupon(), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCoupon(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateCouponMap(requestMap, true)){
                    Coupon couponName = couponRepository.findByNameCoupon(requestMap.get(CafeConstants.COUPON));
                    Optional<Coupon> optional = couponRepository.findById(Long.parseLong(requestMap.get("id")));
                    if(optional.isPresent()){
                        Coupon coupon = getCouponFromMap(requestMap, true);
                        if (Objects.isNull(couponName)) {
                            couponRepository.save(coupon);
                            return CafeUtils.getResponseEntity("Coupon updated successfully", HttpStatus.OK);
                        }
                        else {
                            return CafeUtils.getResponseEntity(CafeConstants.NAMECOUPON_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                        }
                    }else {
                        return CafeUtils.getResponseEntity("Coupon id does not exist", HttpStatus.OK);
                    }
                }else{
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCoupon(Long id) {
        try{
            if(jwtRequestFilter.isAdmin()){
                Optional<Coupon> optional = couponRepository.findById(id);
                if(optional.isPresent()){
                    couponRepository.deleteById(id);
                    return CafeUtils.getResponseEntity("Coupon deleted successfully", HttpStatus.OK);
                }else{
                    return CafeUtils.getResponseEntity("Coupon id does not exist", HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<CouponWrapper> getCouponById(Long id) {
        try{
            return new ResponseEntity<>(couponRepository.getCouponById(id), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new CouponWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
