package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.dto.DashboardResumen;
import com.vitaNova.vitaNova.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class DashboardController {

    private static final DateTimeFormatter SUBTITLE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));

    private final DashboardService dashboardService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardResumen resumen = dashboardService.obtenerResumen();

        model.addAttribute("dashboard", resumen);
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Resumen operativo para " + LocalDate.now().format(SUBTITLE_FORMATTER));
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");

        return "dashboard/Dashboard";
    }
}
