package com.example.to_dolist.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.repository.ITaskRepository;

import java.util.List;

import javax.inject.Inject;

public class GetCategoriesUseCase {
    private final ITaskRepository repository;

    @Inject
    public GetCategoriesUseCase(ITaskRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Category>> execute() {
        return repository.getAllCategories();
    }

    public void addCategory(Category category) {
        repository.insertCategory(category);
    }

    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }
}
