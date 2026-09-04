package com.bank_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankController {

    @GetMapping("/api/hello")
    public String hello(){
        return "Bank Management System Running";

    }

}
