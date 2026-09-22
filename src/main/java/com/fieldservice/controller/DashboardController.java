package com.fieldservice.controller;

import com.fieldservice.domain.enums.TicketFilter;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.security.CustomUserDetails;
import com.fieldservice.service.TechnicianService;
import com.fieldservice.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final TicketService ticketService;
    private final TechnicianService technicianService;

    public DashboardController(TicketService ticketService, TechnicianService technicianService) {
        this.ticketService = ticketService;
        this.technicianService = technicianService;
    }

    @GetMapping("/inicio")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        Map<TicketFilter, Long> counts = ticketService.getFilterCounts(technician);
        List<Ticket> priorityTickets = ticketService.findPriorityTickets(technician);

        model.addAttribute("technician", technician);
        model.addAttribute("counts", counts);
        model.addAttribute("priorityTickets", priorityTickets);
        model.addAttribute("activePage", "inicio");

        return "dashboard/inicio";
    }
}
