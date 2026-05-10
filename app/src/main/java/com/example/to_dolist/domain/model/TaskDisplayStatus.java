package com.example.to_dolist.domain.model;

/**
 * UI-facing status: includes derived {@link #OVERDUE} from due date.
 */
public enum TaskDisplayStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}
