
package com.coffee.controller;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Bill;
import com.coffee.service.BillService;
import com.coffee.utils.CafeUtils;
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
@RequestMapping("/api/v1/bill")
public class BillController {

    @Autowired
    BillService billService;

    @Operation(
            summary = "Generate a new Bill",
            description = "Endpoint to generate a new bill."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/generateBill")
    public ResponseEntity<String> generateBill(@RequestBody Map<String, Object> requestMap){
        try{
            return billService.generateBill(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get Bills",
            description = "Endpoint to get bills."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/getBill")
    public ResponseEntity<List<Bill>> getBills(){
        try{
            return billService.getBills();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get a pdf of Bills",
            description = "Endpoint to get a pdf of bills."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/getPdf")
    public ResponseEntity<byte[]> getPdf(@RequestBody Map<String, Object> requestMap) {
        try {
            return billService.getPdf(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Delete a Bill",
            description = "Endpoint to delete a bill."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Integer id){
        try{
            return billService.deleteBill(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/applyCoupon")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody Map<String, Object> requestMap) {
        try {
            return billService.applyCoupon(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", CafeConstants.SOMETHING_WENT_WRONG));
    }
}