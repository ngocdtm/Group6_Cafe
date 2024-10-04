package com.coffee.controller;

import com.coffee.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard details",
            description = "Fetches the count and other dashboard information."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/details")
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            return dashboardService.getCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(Map.of("message", "Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
