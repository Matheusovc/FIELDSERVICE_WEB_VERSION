package com.fieldservice.domain.model;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = true)
    private Technician technician;

    public Ticket() {
    }

    public Ticket(String number, String title, String description, String customerName,
                  String address, Priority priority, TicketStatus status,
                  LocalDateTime createdAt, Technician technician) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.customerName = customerName;
        this.address = address;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.technician = technician;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        LocalDate today = LocalDate.now();
        LocalDate ticketDate = createdAt.toLocalDate();
        long daysDiff = ChronoUnit.DAYS.between(ticketDate, today);

        if (daysDiff == 0) {
            return "hoje";
        } else if (daysDiff == 1) {
            return "ontem";
        } else if (daysDiff > 1) {
            return daysDiff + " dias atrás";
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public String getFormattedDateTime() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean isPriorityHighOrCritical() {
        return priority == Priority.HIGH || priority == Priority.CRITICAL;
    }

    public boolean isPending() {
        return status == TicketStatus.OPEN || status == TicketStatus.ASSIGNED ||
               status == TicketStatus.ACCEPTED || status == TicketStatus.TRAVELING;
    }

    public boolean isInProgress() {
        return status == TicketStatus.ON_SITE || status == TicketStatus.IN_PROGRESS ||
               status == TicketStatus.WAITING_CONFIRMATION;
    }

    public boolean isCompleted() {
        return status == TicketStatus.COMPLETED || status == TicketStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id) || Objects.equals(number, ticket.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }
}
