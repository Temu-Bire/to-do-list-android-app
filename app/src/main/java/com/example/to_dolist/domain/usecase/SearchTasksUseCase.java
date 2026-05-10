package com.example.to_dolist.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import java.util.List;

import javax.inject.Inject;

public class SearchTasksUseCase {
    private final ITaskRepository repository;

    @Inject
    public SearchTasksUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Task>> execute(String query) {
        return repository.searchTasks(query == null ? "" : query.trim());
    }
}
