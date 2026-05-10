package com.example.to_dolist.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "subtasks",
    foreignKeys = @ForeignKey(
        entity = TaskEntity.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.CASCADE   // auto-delete subtasks when parent task is deleted
    ),
    indices = {@Index("taskId")}
)
public class SubtaskEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int taskId;
    private String title;
    private boolean completed;

    public SubtaskEntity(int taskId, String title, boolean completed) {
        this.taskId = taskId;
        this.title = title;
        this.completed = completed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
