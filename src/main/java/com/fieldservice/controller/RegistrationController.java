package com.fieldservice.controller;

import com.fieldservice.service.TechnicianService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

/**
 * Cadastro de novos usuários (técnicos). Página pública, acessível sem login.
 */
@Controller
public class RegistrationController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final TechnicianService technicianService;

    public RegistrationController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    @GetMapping("/cadastro")
    public String showForm() {
        // Se já estiver autenticado, não faz sentido cadastrar: vai para o início.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/inicio";
        }
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String register(@RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "email", required = false) String email,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null ? "" : confirmPassword;

        // Mantém os valores digitados (exceto senha) caso a validação falhe.
        model.addAttribute("name", name);
        model.addAttribute("email", email);

        String error = validate(name, email, password, confirmPassword);
        if (error != null) {
            model.addAttribute("errorMessage", error);
            return "auth/cadastro";
        }

        technicianService.register(name, email, password);

        redirectAttributes.addFlashAttribute("successMessage",
                "Conta criada com sucesso! Faça login para continuar.");
        return "redirect:/login";
    }

    private String validate(String name, String email, String password, String confirmPassword) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return "Preencha todos os campos.";
        }
        if (name.length() < 3) {
            return "Informe o nome completo (mínimo 3 caracteres).";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Informe um e-mail válido.";
        }
        if (password.length() < 6) {
            return "A senha deve ter ao menos 6 caracteres.";
        }
        if (!password.equals(confirmPassword)) {
            return "As senhas não coincidem.";
        }
        if (technicianService.emailExists(email)) {
            return "Este e-mail já está cadastrado.";
        }
        return null;
    }
}
