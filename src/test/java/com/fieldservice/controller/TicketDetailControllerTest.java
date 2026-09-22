package com.fieldservice.controller;

import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("RF06: Visualizar detalhes do chamado com linha do tempo")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldRenderTicketDetails() throws Exception {
        Ticket ticket = ticketRepository.findByNumber("#1028").orElseThrow();

        mockMvc.perform(get("/chamados/" + ticket.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/detail"))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("timelineSteps"))
                .andExpect(content().string(containsString("#1028")))
                .andExpect(content().string(containsString("Servidor indisponível")))
                .andExpect(content().string(containsString("Empresa XYZ")))
                .andExpect(content().string(containsString("Linha do tempo")))
                .andExpect(content().string(containsString("Aceitar chamado")));
    }

    @Test
    @DisplayName("RF07 / RN008: Aceitar chamado atribuído")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldAcceptTicket() throws Exception {
        Ticket ticket = ticketRepository.findByNumber("#1028").orElseThrow();
        assertEquals(TicketStatus.ASSIGNED, ticket.getStatus());

        mockMvc.perform(post("/chamados/" + ticket.getId() + "/aceitar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chamados/" + ticket.getId()))
                .andExpect(flash().attributeExists("successMessage"));

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    @DisplayName("RF08 / RN007: Avançar fluxo de status pelas etapas completas até a conclusão")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldAdvanceThroughAllStatusSteps() throws Exception {
        Ticket ticket = ticketRepository.findByNumber("#1028").orElseThrow();

        // 1. Aceitar
        mockMvc.perform(post("/chamados/" + ticket.getId() + "/aceitar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(TicketStatus.ACCEPTED, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());

        // 2. Iniciar Deslocamento -> TRAVELING
        mockMvc.perform(post("/chamados/" + ticket.getId() + "/avancar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(TicketStatus.TRAVELING, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());

        // 3. Registrar Chegada -> ON_SITE
        mockMvc.perform(post("/chamados/" + ticket.getId() + "/avancar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(TicketStatus.ON_SITE, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());

        // 4. Iniciar Atendimento -> IN_PROGRESS
        mockMvc.perform(post("/chamados/" + ticket.getId() + "/avancar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());

        // 5. Finalizar Atendimento -> COMPLETED
        mockMvc.perform(post("/chamados/" + ticket.getId() + "/avancar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(TicketStatus.COMPLETED, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());

        // 6. Chamado Concluído na tela exibe 'Atendimento Concluído' e não permite avançar (RN009)
        mockMvc.perform(get("/chamados/" + ticket.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Atendimento Concluído")));
    }
}
