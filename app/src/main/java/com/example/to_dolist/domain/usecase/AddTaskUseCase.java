package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import javax.inject.Inject;

public class AddTaskUseCase {
    private final ITaskRepository repository;

    @Inject
    public AddTaskUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates and inserts a task.
     * @throws IllegalArgumentException if title is blank
     */
    public void execute(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        repository.insertTask(task);
    }
}
