package com.example.to_dolist.domain.model;

public enum Priority {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Resolves persisted English labels ("High"), enum names ("HIGH"), or UI translations. */
    public static Priority fromLabel(String label) {
        if (label == null || label.isEmpty()) return LOW;
        String t = label.trim();
        try {
            return Priority.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            /* fall through */
        }
        for (Priority p : values()) {
            if (p.label.equalsIgnoreCase(t)) return p;
        }
        return LOW;
    }
}
