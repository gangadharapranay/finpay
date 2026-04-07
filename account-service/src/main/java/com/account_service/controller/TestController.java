package com.account_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/test")
public class TestController {
    @GetMapping
    public String testApi()
    {
        return "I am Sample String of Test API";
    }
}
