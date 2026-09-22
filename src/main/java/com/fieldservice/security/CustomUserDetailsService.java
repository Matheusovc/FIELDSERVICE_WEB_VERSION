package com.fieldservice.security;

import com.fieldservice.domain.model.Technician;
import com.fieldservice.repository.TechnicianRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TechnicianRepository technicianRepository;

    public CustomUserDetailsService(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("E-mail não informado");
        }

        Technician technician = technicianRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Técnico não encontrado com o e-mail: " + email));

        return new CustomUserDetails(technician);
    }
}
