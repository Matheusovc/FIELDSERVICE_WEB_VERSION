package com.fieldservice.domain.enums;

import java.util.List;

public enum TicketFilter {
    ALL("Todos", List.of(TicketStatus.values())),
    PENDING("Pendentes", List.of(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.ACCEPTED, TicketStatus.TRAVELING)),
    IN_PROGRESS("Em atendimento", List.of(TicketStatus.ON_SITE, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_CONFIRMATION)),
    COMPLETED("Concluídos", List.of(TicketStatus.COMPLETED, TicketStatus.CANCELLED));

    private final String label;
    private final List<TicketStatus> statuses;

    TicketFilter(String label, List<TicketStatus> statuses) {
        this.label = label;
        this.statuses = statuses;
    }

    public String getLabel() {
        return label;
    }

    public List<TicketStatus> getStatuses() {
        return statuses;
    }

    public boolean matches(TicketStatus status) {
        if (this == ALL) {
            return true;
        }
        return statuses.contains(status);
    }
}
