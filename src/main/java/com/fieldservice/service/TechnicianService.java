package com.fieldservice.service;

import com.fieldservice.domain.model.Technician;
import com.fieldservice.repository.TechnicianRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final PasswordEncoder passwordEncoder;

    public TechnicianService(TechnicianRepository technicianRepository,
                             PasswordEncoder passwordEncoder) {
        this.technicianRepository = technicianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Verifica se já existe um técnico cadastrado com o e-mail informado. */
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) return false;
        return technicianRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Cadastra um novo técnico com a senha criptografada (BCrypt).
     * O e-mail é normalizado (minúsculo, sem espaços) e o cargo padrão é "Técnico de Campo".
     */
    @Transactional
    public Technician register(String name, String email, String rawPassword) {
        Technician technician = new Technician(
                name.trim(),
                email.trim().toLowerCase(),
                passwordEncoder.encode(rawPassword),
                "Técnico de Campo"
        );
        return technicianRepository.save(technician);
    }

    public Optional<Technician> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return technicianRepository.findByEmail(email.trim().toLowerCase());
    }

    public Optional<Technician> findById(Long id) {
        if (id == null) return Optional.empty();
        return technicianRepository.findById(id);
    }

    @Transactional
    public Technician save(Technician technician) {
        return technicianRepository.save(technician);
    }
}
