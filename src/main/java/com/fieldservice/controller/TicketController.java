package com.fieldservice.controller;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketFilter;
import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.security.CustomUserDetails;
import com.fieldservice.service.TechnicianService;
import com.fieldservice.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class TicketController {

    private final TicketService ticketService;
    private final TechnicianService technicianService;

    // Etapas padrão da linha do tempo no atendimento (RN007)
    private static final List<TicketStatus> TIMELINE_STEPS = List.of(
            TicketStatus.ASSIGNED,
            TicketStatus.ACCEPTED,
            TicketStatus.TRAVELING,
            TicketStatus.ON_SITE,
            TicketStatus.IN_PROGRESS,
            TicketStatus.COMPLETED
    );

    public TicketController(TicketService ticketService, TechnicianService technicianService) {
        this.ticketService = ticketService;
        this.technicianService = technicianService;
    }

    @GetMapping("/chamados")
    public String listTickets(
            @RequestParam(value = "filtro", defaultValue = "ALL") String filtroParam,
            @RequestParam(value = "erro", required = false) String erro,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        TicketFilter currentFilter;
        try {
            currentFilter = TicketFilter.valueOf(filtroParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            currentFilter = TicketFilter.ALL;
        }

        List<Ticket> tickets = ticketService.findByFilter(technician, currentFilter);
        Map<TicketFilter, Long> counts = ticketService.getFilterCounts(technician);

        model.addAttribute("technician", technician);
        model.addAttribute("tickets", tickets);
        model.addAttribute("counts", counts);
        model.addAttribute("currentFilter", currentFilter);
        model.addAttribute("activePage", "chamados");

        if (erro != null) {
            model.addAttribute("errorMessage", "Ocorreu um erro ao carregar os chamados. Tente novamente.");
        }

        return "tickets/list";
    }

    /**
     * Exibe o formulário de cadastro manual de chamado.
     * A rota literal "/chamados/novo" tem precedência sobre "/chamados/{id}".
     */
    @GetMapping("/chamados/novo")
    public String newTicketForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        model.addAttribute("technician", technician);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("activePage", "chamados");
        return "tickets/new";
    }

    /**
     * Processa o cadastro manual de chamado, atribuindo-o ao técnico logado.
     */
    @PostMapping("/chamados/novo")
    public String createTicket(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "priority", required = false) String priorityParam,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        title = title == null ? "" : title.trim();
        customerName = customerName == null ? "" : customerName.trim();
        address = address == null ? "" : address.trim();
        description = description == null ? "" : description.trim();

        // Preserva os valores digitados caso a validação falhe.
        model.addAttribute("technician", technician);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("activePage", "chamados");
        model.addAttribute("fTitle", title);
        model.addAttribute("fCustomerName", customerName);
        model.addAttribute("fAddress", address);
        model.addAttribute("fDescription", description);
        model.addAttribute("fPriority", priorityParam);

        String error = null;
        Priority priority = null;
        if (title.isBlank() || customerName.isBlank() || address.isBlank()
                || description.isBlank() || priorityParam == null || priorityParam.isBlank()) {
            error = "Preencha todos os campos.";
        } else {
            try {
                priority = Priority.valueOf(priorityParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                error = "Selecione uma prioridade válida.";
            }
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
            return "tickets/new";
        }

        Ticket created = ticketService.createManualTicket(
                technician, title, description, customerName, address, priority);

        redirectAttributes.addFlashAttribute("successMessage",
                "Chamado " + created.getNumber() + " cadastrado com sucesso!");
        return "redirect:/chamados/" + created.getId();
    }

    @GetMapping("/chamados/{id}")
    public String ticketDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        Optional<Ticket> ticketOpt = ticketService.findByIdAndTechnician(id, technician);

        if (ticketOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chamado não encontrado ou você não tem permissão para visualizá-lo.");
            return "redirect:/chamados";
        }

        Ticket ticket = ticketOpt.get();

        model.addAttribute("technician", technician);
        model.addAttribute("ticket", ticket);
        model.addAttribute("timelineSteps", TIMELINE_STEPS);
        model.addAttribute("activePage", "chamados");

        return "tickets/detail";
    }

    @PostMapping("/chamados/{id}/avancar")
    public String advanceTicketStatus(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        try {
            Ticket updatedTicket = ticketService.advanceStatus(id, technician);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Status avançado com sucesso para: " + updatedTicket.getStatus().getDescription());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao avançar status: " + e.getMessage());
        }

        return "redirect:/chamados/" + id;
    }

    @PostMapping("/chamados/{id}/aceitar")
    public String acceptTicket(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Technician technician = technicianService.findByEmail(userDetails.getUsername())
                .orElse(userDetails.getTechnician());

        try {
            Ticket updatedTicket = ticketService.acceptTicket(id, technician);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Chamado aceito com sucesso! Próximo passo: Iniciar deslocamento.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao aceitar chamado: " + e.getMessage());
        }

        return "redirect:/chamados/" + id;
    }
}
