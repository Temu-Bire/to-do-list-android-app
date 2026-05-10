package com.example.to_dolist.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure domain model — no Room annotations, no Android dependencies.
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private long dueDate;
    private boolean completed;
    private Integer categoryId;          // nullable
    private boolean reminderEnabled;
    private boolean recurring;
    private String recurrenceRule;       // e.g. "DAILY", "WEEKLY"
    private List<Subtask> subtasks;

    public Task(int id, String title, String description, Priority priority,
                long dueDate, boolean completed, Integer categoryId,
                boolean reminderEnabled, boolean recurring, String recurrenceRule) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
        this.categoryId = categoryId;
        this.reminderEnabled = reminderEnabled;
        this.recurring = recurring;
        this.recurrenceRule = recurrenceRule;
        this.subtasks = new ArrayList<>();
    }

    // --- Computed helpers ---

    public boolean isOverdue() {
        return !completed && dueDate < System.currentTimeMillis();
    }

    public int getSubtaskCompletedCount() {
        if (subtasks == null) return 0;
        int count = 0;
        for (Subtask s : subtasks) if (s.isCompleted()) count++;
        return count;
    }

    // --- Getters / Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public String getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(String recurrenceRule) { this.recurrenceRule = recurrenceRule; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }
}
