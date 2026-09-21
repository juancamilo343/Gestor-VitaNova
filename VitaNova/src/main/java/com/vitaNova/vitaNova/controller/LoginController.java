package com.vitaNova.vitaNova.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {

        model.addAttribute("pageTitle", "Iniciar sesión");

        return "login";
    }
}