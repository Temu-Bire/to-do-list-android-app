package com.example.to_dolist.domain.model;

public class Subtask {
    private int id;
    private final int taskId;
    private String title;
    private boolean completed;

    public Subtask(int id, int taskId, String title, boolean completed) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.completed = completed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
