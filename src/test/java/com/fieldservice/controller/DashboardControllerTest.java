package com.fieldservice.controller;

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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RF03 / RN006: Usuário autenticado acessa /inicio e recebe métricas e chamados prioritários")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldRenderDashboardWithMetricsAndPriorityTickets() throws Exception {
        mockMvc.perform(get("/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/inicio"))
                .andExpect(model().attributeExists("technician"))
                .andExpect(model().attributeExists("counts"))
                .andExpect(model().attributeExists("priorityTickets"))
                .andExpect(model().attribute("activePage", "inicio"))
                .andExpect(content().string(containsString("João")))
                .andExpect(content().string(containsString("Aqui está o resumo dos seus atendimentos.")))
                .andExpect(content().string(containsString("Pendentes")))
                .andExpect(content().string(containsString("Em atendimento")))
                .andExpect(content().string(containsString("Concluídos")))
                .andExpect(content().string(containsString("Chamados prioritários")))
                .andExpect(content().string(containsString("#1031")))
                .andExpect(content().string(containsString("Empresa Omega")));
    }
}
