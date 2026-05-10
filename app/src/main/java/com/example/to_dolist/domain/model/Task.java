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
    private TaskWorkflowStatus workflowStatus;
    private int sortOrder;
    private List<Subtask> subtasks;

    public Task(int id, String title, String description, Priority priority,
                long dueDate, boolean completed, Integer categoryId,
                boolean reminderEnabled, boolean recurring, String recurrenceRule) {
        this(id, title, description, priority, dueDate, completed, categoryId,
                reminderEnabled, recurring, recurrenceRule, TaskWorkflowStatus.PENDING, 0);
    }

    public Task(int id, String title, String description, Priority priority,
                long dueDate, boolean completed, Integer categoryId,
                boolean reminderEnabled, boolean recurring, String recurrenceRule,
                TaskWorkflowStatus workflowStatus, int sortOrder) {
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
        this.workflowStatus = workflowStatus != null ? workflowStatus : TaskWorkflowStatus.PENDING;
        this.sortOrder = sortOrder;
        this.subtasks = new ArrayList<>();
    }

    // --- Computed helpers ---

    public boolean isOverdue() {
        return !completed && dueDate < System.currentTimeMillis();
    }

    /** Status for chips and filters: completed and overdue override workflow. */
    public TaskDisplayStatus getDisplayStatus() {
        if (completed) return TaskDisplayStatus.COMPLETED;
        if (isOverdue()) return TaskDisplayStatus.OVERDUE;
        if (workflowStatus == TaskWorkflowStatus.IN_PROGRESS) return TaskDisplayStatus.IN_PROGRESS;
        return TaskDisplayStatus.PENDING;
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

    public TaskWorkflowStatus getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(TaskWorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus != null ? workflowStatus : TaskWorkflowStatus.PENDING;
    }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }
}
