package com.coffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentViewController {

    @GetMapping("/success")
    public String successView() {
        return "success";
    }

    @GetMapping("/error")
    public String errorView() {
        return "error";
    }
}