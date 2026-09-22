package com.example.textile.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPagesController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}