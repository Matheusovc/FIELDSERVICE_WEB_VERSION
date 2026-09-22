package com.fieldservice.domain;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketFilter;
import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.repository.TechnicianRepository;
import com.fieldservice.repository.TicketRepository;
import com.fieldservice.service.TechnicianService;
import com.fieldservice.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DomainAndSeedIntegrationTest {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private TicketService ticketService;

    @Test
    @DisplayName("Deve carregar o seed de técnicos e chamados corretamente no H2")
    void shouldLoadSeedDataProperly() {
        assertEquals(2, technicianRepository.count(), "Devem existir 2 técnicos cadastrados");
        assertEquals(7, ticketRepository.count(), "Devem existir 7 chamados cadastrados");

        Optional<Technician> joaoOpt = technicianService.findByEmail("tecnico@fieldservice.com");
        assertTrue(joaoOpt.isPresent(), "Técnico João Silva deve estar presente");
        Technician joao = joaoOpt.get();
        assertEquals("João Silva", joao.getName());
        assertEquals("João", joao.getFirstName());
        assertEquals("JS", joao.getInitials());
        assertEquals("Técnico de Campo", joao.getRole());

        Optional<Technician> matheusOpt = technicianService.findByEmail("matheus@fieldservice.com");
        assertTrue(matheusOpt.isPresent(), "Técnico Matheus Carvalho deve estar presente");
        assertEquals("MC", matheusOpt.get().getInitials());
    }

    @Test
    @DisplayName("Deve filtrar chamados por situação e contar corretamente (RN005 / RF03 / RF05)")
    void shouldFilterTicketsAndCalculateCounts() {
        Technician joao = technicianService.findByEmail("tecnico@fieldservice.com").orElseThrow();

        Map<TicketFilter, Long> counts = ticketService.getFilterCounts(joao);
        assertEquals(7, counts.get(TicketFilter.ALL));
        // Pendentes: #1028 (ASSIGNED), #1029 (ACCEPTED), #1031 (OPEN), #1034 (TRAVELING) -> 4
        assertEquals(4, counts.get(TicketFilter.PENDING));
        // Em atendimento: #1032 (IN_PROGRESS) -> 1
        assertEquals(1, counts.get(TicketFilter.IN_PROGRESS));
        // Concluídos: #1030 (COMPLETED), #1033 (COMPLETED) -> 2
        assertEquals(2, counts.get(TicketFilter.COMPLETED));

        List<Ticket> pendingTickets = ticketService.findByFilter(joao, TicketFilter.PENDING);
        assertEquals(4, pendingTickets.size());
    }

    @Test
    @DisplayName("Deve listar chamados prioritários HIGH e CRITICAL não concluídos limitados a 3 (RN006)")
    void shouldReturnPriorityTicketsOrdered() {
        Technician joao = technicianService.findByEmail("tecnico@fieldservice.com").orElseThrow();

        List<Ticket> priorityTickets = ticketService.findPriorityTickets(joao);
        assertNotNull(priorityTickets);
        assertTrue(priorityTickets.size() <= 3, "Deve limitar a no máximo 3 chamados prioritários");
        assertFalse(priorityTickets.isEmpty());

        // Primeiro deve ser CRITICAL (#1031)
        assertEquals(Priority.CRITICAL, priorityTickets.get(0).getPriority());
        assertEquals("#1031", priorityTickets.get(0).getNumber());

        // Segundo deve ser HIGH
        assertEquals(Priority.HIGH, priorityTickets.get(1).getPriority());
    }

    @Test
    @DisplayName("Deve validar a máquina de estados e avanço de status (RN007 / RN008 / RN009)")
    void shouldAdvanceStatusCorrectly() {
        Technician joao = technicianService.findByEmail("tecnico@fieldservice.com").orElseThrow();

        // #1028 está ASSIGNED
        Ticket ticket1028 = ticketRepository.findByNumber("#1028").orElseThrow();
        assertEquals(TicketStatus.ASSIGNED, ticket1028.getStatus());

        // Aceitar -> ACCEPTED
        Ticket accepted = ticketService.acceptTicket(ticket1028.getId(), joao);
        assertEquals(TicketStatus.ACCEPTED, accepted.getStatus());

        // Iniciar deslocamento -> TRAVELING
        Ticket traveling = ticketService.advanceStatus(accepted.getId(), joao);
        assertEquals(TicketStatus.TRAVELING, traveling.getStatus());

        // Registrar chegada -> ON_SITE
        Ticket onSite = ticketService.advanceStatus(traveling.getId(), joao);
        assertEquals(TicketStatus.ON_SITE, onSite.getStatus());

        // Iniciar atendimento -> IN_PROGRESS
        Ticket inProgress = ticketService.advanceStatus(onSite.getId(), joao);
        assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());

        // Finalizar -> COMPLETED
        Ticket completed = ticketService.advanceStatus(inProgress.getId(), joao);
        assertEquals(TicketStatus.COMPLETED, completed.getStatus());
        assertTrue(completed.getStatus().isTerminal());

        // Tentar avançar chamado concluído deve lançar erro (RN009)
        assertThrows(IllegalStateException.class, () -> ticketService.advanceStatus(completed.getId(), joao));
    }
}
