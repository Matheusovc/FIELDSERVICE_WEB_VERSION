package com.fieldservice.domain.enums;

public enum Priority {
    LOW("Baixa", "badge-priority-low"),
    MEDIUM("Média", "badge-priority-medium"),
    HIGH("Alta", "badge-priority-high"),
    CRITICAL("Crítica", "badge-priority-critical");

    private final String description;
    private final String badgeClass;

    Priority(String description, String badgeClass) {
        this.description = description;
        this.badgeClass = badgeClass;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
