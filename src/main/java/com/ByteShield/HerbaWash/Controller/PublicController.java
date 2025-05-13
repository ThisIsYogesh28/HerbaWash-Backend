package com.ByteShield.HerbaWash.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    @GetMapping
    public String pub(){
        return "public";
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
