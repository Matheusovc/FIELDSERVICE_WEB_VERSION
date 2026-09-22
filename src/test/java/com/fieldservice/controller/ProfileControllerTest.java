package com.fieldservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RF09 - RF12: Acessa /perfil e visualiza dados do técnico, cargo, status, versão e modais de configuração")
    @WithUserDetails(value = "tecnico@fieldservice.com", userDetailsServiceBeanName = "customUserDetailsService")
    void shouldRenderProfilePageWithTechnicianDataAndModals() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/perfil"))
                .andExpect(model().attributeExists("technician"))
                .andExpect(model().attribute("appVersion", "2.0_build0.1"))
                .andExpect(model().attribute("activePage", "perfil"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(containsString("Técnico de Campo")))
                .andExpect(content().string(containsString("tecnico@fieldservice.com")))
                .andExpect(content().string(containsString("Disponível para novos chamados")))
                .andExpect(content().string(containsString("Aparência e Tema")))
                .andExpect(content().string(containsString("Notificações")))
                .andExpect(content().string(containsString("2.0_build0.1")));
    }
}
