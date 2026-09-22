package com.fieldservice.security;

import com.fieldservice.domain.model.Technician;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Technician technician;

    public CustomUserDetails(Technician technician) {
        this.technician = technician;
    }

    public Technician getTechnician() {
        return technician;
    }

    public Long getId() {
        return technician.getId();
    }

    public String getName() {
        return technician.getName();
    }

    public String getFirstName() {
        return technician.getFirstName();
    }

    public String getInitials() {
        return technician.getInitials();
    }

    public String getTechnicianRole() {
        return technician.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));
    }

    @Override
    public String getPassword() {
        return technician.getPassword();
    }

    @Override
    public String getUsername() {
        return technician.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
