package com.fieldservice.controller;

import com.fieldservice.domain.enums.TicketFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RF04 / RF05: Acessa /chamados com filtro padrão ALL e exibe lista com contadores")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldRenderTicketListWithAllFilter() throws Exception {
        mockMvc.perform(get("/chamados"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/list"))
                .andExpect(model().attribute("currentFilter", TicketFilter.ALL))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attributeExists("counts"))
                .andExpect(content().string(containsString("Chamados de Campo")))
                .andExpect(content().string(containsString("#1028")))
                .andExpect(content().string(containsString("#1031")))
                .andExpect(content().string(containsString("Todos")))
                .andExpect(content().string(containsString("Pendentes")));
    }

    @Test
    @DisplayName("RF05: Filtra chamados por PENDING e exibe 4 chamados")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldFilterPendingTickets() throws Exception {
        mockMvc.perform(get("/chamados").param("filtro", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/list"))
                .andExpect(model().attribute("currentFilter", TicketFilter.PENDING))
                .andExpect(content().string(containsString("#1028"))) // ASSIGNED
                .andExpect(content().string(containsString("#1029"))) // ACCEPTED
                .andExpect(content().string(containsString("#1031"))) // OPEN
                .andExpect(content().string(containsString("#1034"))); // TRAVELING
    }

    @Test
    @DisplayName("RF05: Filtra chamados por IN_PROGRESS e exibe 1 chamado")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldFilterInProgressTickets() throws Exception {
        mockMvc.perform(get("/chamados").param("filtro", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/list"))
                .andExpect(model().attribute("currentFilter", TicketFilter.IN_PROGRESS))
                .andExpect(content().string(containsString("#1032"))); // IN_PROGRESS
    }

    @Test
    @DisplayName("RF05: Filtra chamados por COMPLETED e exibe 2 chamados")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldFilterCompletedTickets() throws Exception {
        mockMvc.perform(get("/chamados").param("filtro", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/list"))
                .andExpect(model().attribute("currentFilter", TicketFilter.COMPLETED))
                .andExpect(content().string(containsString("#1030"))) // COMPLETED
                .andExpect(content().string(containsString("#1033"))); // COMPLETED
    }
}
