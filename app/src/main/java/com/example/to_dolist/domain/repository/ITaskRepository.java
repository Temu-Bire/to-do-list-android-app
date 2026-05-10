package com.example.to_dolist.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Task;

import java.util.List;

/**
 * Contract between domain and data layers.
 * ViewModels and Use Cases depend only on this interface — never on the implementation.
 */
public interface ITaskRepository {

    // ─── Tasks ───────────────────────────────────────────────────────────────

    LiveData<List<Task>> getAllTasks();

    LiveData<List<Task>> getCompletedTasks();

    LiveData<List<Task>> getTasksSortedByPriority();

    LiveData<List<Task>> getTasksByCategory(int categoryId);

    LiveData<List<Task>> searchTasks(String query);

    LiveData<List<Task>> getOverdueTasks();

    void insertTask(Task task);

    void updateTask(Task task);

    void deleteTask(Task task);

    // ─── Categories ──────────────────────────────────────────────────────────

    LiveData<List<Category>> getAllCategories();

    void insertCategory(Category category);

    void deleteCategory(Category category);
}
