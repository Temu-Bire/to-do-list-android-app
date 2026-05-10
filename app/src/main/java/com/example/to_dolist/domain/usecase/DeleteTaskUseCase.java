package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import javax.inject.Inject;

public class DeleteTaskUseCase {
    private final ITaskRepository repository;

    @Inject
    public DeleteTaskUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public void execute(Task task) {
        repository.deleteTask(task);
    }
}
