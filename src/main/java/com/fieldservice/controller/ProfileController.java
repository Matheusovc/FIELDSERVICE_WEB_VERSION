package com.fieldservice.controller;

import com.fieldservice.domain.model.Technician;
import com.fieldservice.security.CustomUserDetails;
import com.fieldservice.service.TechnicianService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final TechnicianService technicianService;
    private static final String APP_VERSION = "2.0_build0.1";

    public ProfileController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    @GetMapping("/perfil")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        model.addAttribute("technician", technician);
        model.addAttribute("appVersion", APP_VERSION);
        model.addAttribute("activePage", "perfil");

        return "profile/perfil";
    }
}
