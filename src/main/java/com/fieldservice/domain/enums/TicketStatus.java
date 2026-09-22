package com.fieldservice.domain.enums;

import java.util.Optional;

public enum TicketStatus {
    OPEN("Aberto", "badge-status-neutral", 0),
    ASSIGNED("Atribuído", "badge-status-neutral", 1),
    ACCEPTED("Aceito", "badge-status-progress", 2),
    TRAVELING("A caminho", "badge-status-progress", 3),
    ON_SITE("No local", "badge-status-progress", 4),
    IN_PROGRESS("Em atendimento", "badge-status-progress", 5),
    WAITING_CONFIRMATION("Aguardando confirmação", "badge-status-warning", 5),
    COMPLETED("Concluído", "badge-status-completed", 6),
    CANCELLED("Cancelado", "badge-status-cancelled", -1);

    private final String description;
    private final String badgeClass;
    private final int stepOrder;

    TicketStatus(String description, String badgeClass, int stepOrder) {
        this.description = description;
        this.badgeClass = badgeClass;
        this.stepOrder = stepOrder;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public Optional<TicketStatus> getNextStatus() {
        return switch (this) {
            case OPEN -> Optional.of(ASSIGNED);
            case ASSIGNED -> Optional.of(ACCEPTED);
            case ACCEPTED -> Optional.of(TRAVELING);
            case TRAVELING -> Optional.of(ON_SITE);
            case ON_SITE -> Optional.of(IN_PROGRESS);
            case IN_PROGRESS -> Optional.of(COMPLETED);
            case WAITING_CONFIRMATION -> Optional.of(COMPLETED);
            case COMPLETED, CANCELLED -> Optional.empty();
        };
    }

    public Optional<String> getNextActionLabel() {
        return switch (this) {
            case OPEN -> Optional.of("Atribuir a mim");
            case ASSIGNED -> Optional.of("Aceitar chamado");
            case ACCEPTED -> Optional.of("Iniciar deslocamento");
            case TRAVELING -> Optional.of("Registrar chegada");
            case ON_SITE -> Optional.of("Iniciar atendimento");
            case IN_PROGRESS -> Optional.of("Finalizar atendimento");
            case WAITING_CONFIRMATION -> Optional.of("Concluir atendimento");
            case COMPLETED, CANCELLED -> Optional.empty();
        };
    }

    public Optional<String> getNextActionIcon() {
        return switch (this) {
            case OPEN -> Optional.of("bi-person-check");
            case ASSIGNED -> Optional.of("bi-check2-circle");
            case ACCEPTED -> Optional.of("bi-truck");
            case TRAVELING -> Optional.of("bi-geo-alt");
            case ON_SITE -> Optional.of("bi-play-circle");
            case IN_PROGRESS -> Optional.of("bi-check2-all");
            case WAITING_CONFIRMATION -> Optional.of("bi-check-circle");
            case COMPLETED, CANCELLED -> Optional.empty();
        };
    }
}
