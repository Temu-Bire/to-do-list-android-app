package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.repository.ITaskRepository;

import javax.inject.Inject;

public class InsertCategoryUseCase {

    private final ITaskRepository repository;

    @Inject
    public InsertCategoryUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public void execute(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        repository.insertCategory(category);
    }
}
