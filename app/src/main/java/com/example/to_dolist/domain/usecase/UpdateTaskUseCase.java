package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import javax.inject.Inject;

public class UpdateTaskUseCase {
    private final ITaskRepository repository;

    @Inject
    public UpdateTaskUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public void execute(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        repository.updateTask(task);
    }
}
