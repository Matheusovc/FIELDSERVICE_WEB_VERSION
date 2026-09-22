package com.fieldservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RN001: Usuário não autenticado tentando acessar telas protegidas deve ser redirecionado para /login")
    void unauthenticatedUserShouldBeRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/inicio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/chamados"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Deve permitir acesso público à página de login")
    void shouldAllowPublicAccessToLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("RN001 / RF01: Login com credenciais válidas deve autenticar com sucesso e redirecionar para /inicio")
    void validLoginShouldAuthenticateAndRedirectToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "tecnico@fieldservice.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    @DisplayName("RN002: Login com credenciais inválidas deve redirecionar para /login?error=invalid")
    void invalidCredentialsShouldRedirectWithError() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "tecnico@fieldservice.com")
                .param("password", "senha_errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=invalid"));

        mockMvc.perform(get("/login?error=invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("RN002: Login com campos vazios deve redirecionar para /login?error=empty")
    void emptyCredentialsShouldRedirectWithEmptyError() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "")
                .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=empty"));

        mockMvc.perform(get("/login?error=empty"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "Informe e-mail e senha"));
    }

    @Test
    @DisplayName("RN003: Logout deve invalidar a sessão e redirecionar para /login?logout")
    @WithMockUser(username = "tecnico@fieldservice.com", roles = {"TECHNICIAN"})
    void logoutShouldRedirectToLoginLogout() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @DisplayName("Página de login exibe mensagem de logout quando parâmetro logout está presente")
    void loginPageDisplaysLogoutMessage() throws Exception {
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("logoutMessage", "Sessão encerrada com sucesso."));
    }
}
