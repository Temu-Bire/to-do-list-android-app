package com.example.to_dolist.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import java.util.List;

import javax.inject.Inject;

public class GetOverdueTasksUseCase {
    private final ITaskRepository repository;

    @Inject
    public GetOverdueTasksUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Task>> execute() {
        return repository.getOverdueTasks();
    }
}
