package com.coffee.controller;

import com.coffee.constants.CafeConstants;
import com.coffee.service.CouponService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.CouponWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupon")
public class CouponController {

    @Autowired
    CouponService couponService;

    @Operation(
            summary = "Add a new coupon",
            description = "Endpoint to add a new coupon to the coupon."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/add")
    public ResponseEntity<String> addCoupon(@RequestBody(required = true) Map<String, String> requestMap){
        try{
            return couponService.addCoupon(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get all coupon",
            description = "Endpoint to get all coupon."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/get")
    public ResponseEntity<List<CouponWrapper>> getAllCoupon(){
        try{
            return couponService.getAllCoupon();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Update a coupon",
            description = "Endpoint to update a coupon."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/update")
    public ResponseEntity<String> updateCoupon(@RequestBody Map<String, String> requestMap){
        try{
            return  couponService.updateCoupon(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Delete a coupon",
            description = "Endpoint to delete a coupon."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteCoupon(@PathVariable Long id){
        try{
            return couponService.deleteCoupon(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get a Coupon by id",
            description = "Endpoint to get a Coupon by id."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/getById/{id}")
    public ResponseEntity<CouponWrapper> getCouponById(@PathVariable Long id){
        try{
            return couponService.getCouponById(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new CouponWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
