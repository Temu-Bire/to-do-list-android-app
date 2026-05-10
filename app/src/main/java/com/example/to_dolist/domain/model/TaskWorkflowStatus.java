package com.example.to_dolist.domain.model;

/**
 * Persisted workflow for incomplete tasks. {@link Task#getDisplayStatus()} also derives
 * {@link TaskDisplayStatus#OVERDUE} and {@link TaskDisplayStatus#COMPLETED} from dates and flags.
 */
public enum TaskWorkflowStatus {
    PENDING,
    IN_PROGRESS;

    public static TaskWorkflowStatus fromKey(String key) {
        if (key == null) return PENDING;
        try {
            return valueOf(key);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }

    public String getKey() {
        return name();
    }
}
