package com.example.demo.controller;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String admin(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("username", principal.getAttribute("name"));
        model.addAttribute("attributes", principal.getAttributes());
        return "admin";
    }
}
