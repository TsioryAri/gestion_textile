package com.example.textile.controller.web;

import com.example.textile.service.CommandeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CommandeService commandeService;

    public DashboardController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", commandeService.obtenirStatistiques());
        return "dashboard";
    }
}