package com.ByteShield.HerbaWash.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class MainController {
    @GetMapping("/profile")
    public String profile(){
        return "profile";
    }
}
