package com.example.to_dolist.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.to_dolist.data.local.dao.CategoryDao;
import com.example.to_dolist.data.local.dao.SubtaskDao;
import com.example.to_dolist.data.local.dao.TaskDao;
import com.example.to_dolist.data.local.entity.CategoryEntity;
import com.example.to_dolist.data.local.entity.TaskEntity;
import com.example.to_dolist.data.mapper.CategoryMapper;
import com.example.to_dolist.data.mapper.SubtaskMapper;
import com.example.to_dolist.data.mapper.TaskMapper;
import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Subtask;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Concrete implementation of ITaskRepository.
 *
 * Senior engineer note: ViewModels and Use Cases never import this class.
 * They only know the ITaskRepository interface — Dependency Inversion Principle.
 */
@Singleton
public class TaskRepositoryImpl implements ITaskRepository {

    private final TaskDao taskDao;
    private final CategoryDao categoryDao;
    private final SubtaskDao subtaskDao;

    // Single-threaded executor — serial writes, no race conditions
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public TaskRepositoryImpl(TaskDao taskDao, CategoryDao categoryDao, SubtaskDao subtaskDao) {
        this.taskDao = taskDao;
        this.categoryDao = categoryDao;
        this.subtaskDao = subtaskDao;
    }

    // ─── Tasks ───────────────────────────────────────────────────────────────

    @Override
    public LiveData<List<Task>> getAllTasks() {
        return Transformations.map(taskDao.getAllTasks(), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getIncompleteTasks() {
        return Transformations.map(taskDao.getIncompleteTasks(), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getInProgressTasks() {
        return Transformations.map(taskDao.getInProgressTasks(), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getCompletedTasks() {
        return Transformations.map(taskDao.getCompletedTasks(), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getTasksSortedByPriority() {
        return Transformations.map(taskDao.getTasksSortedByPriority(), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getTasksByCategory(int categoryId) {
        return Transformations.map(taskDao.getTasksByCategory(categoryId), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> searchTasks(String query) {
        return Transformations.map(taskDao.searchTasks(query), TaskMapper::toDomainList);
    }

    @Override
    public LiveData<List<Task>> getOverdueTasks() {
        // Pass current time so Room can evaluate which tasks are past-due
        return Transformations.map(
                taskDao.getOverdueTasks(System.currentTimeMillis()),
                TaskMapper::toDomainList
        );
    }

    @Override
    public void insertTask(Task task) {
        executor.execute(() -> {
            TaskEntity entity = TaskMapper.toEntity(task);
            if (entity.getId() == 0) {
                entity.setSortOrder(taskDao.peekNextSortOrder());
            }
            taskDao.insert(entity);
        });
    }

    @Override
    public void updateTask(Task task) {
        executor.execute(() -> {
            taskDao.update(TaskMapper.toEntity(task));
            // Replace subtasks atomically — delete old, insert new
            if (task.getSubtasks() != null) {
                subtaskDao.deleteAllForTask(task.getId());
                for (Subtask s : task.getSubtasks()) {
                    subtaskDao.insert(SubtaskMapper.toEntity(s));
                }
            }
        });
    }

    @Override
    public void deleteTask(Task task) {
        // Subtasks cascade-deleted by Room foreign key definition
        executor.execute(() -> taskDao.delete(TaskMapper.toEntity(task)));
    }

    @Override
    public void updateSortOrders(List<Task> orderedTasks) {
        if (orderedTasks == null || orderedTasks.isEmpty()) return;
        executor.execute(() -> {
            for (int i = 0; i < orderedTasks.size(); i++) {
                Task t = orderedTasks.get(i);
                taskDao.updateSortOrder(t.getId(), (i + 1) * 1000);
            }
        });
    }

    // ─── Categories ──────────────────────────────────────────────────────────

    @Override
    public LiveData<List<Category>> getAllCategories() {
        return Transformations.map(categoryDao.getAllCategories(), entities -> {
            List<Category> result = new ArrayList<>();
            for (CategoryEntity e : entities) {
                result.add(CategoryMapper.toDomain(e));
            }
            return result;
        });
    }

    @Override
    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insert(CategoryMapper.toEntity(category)));
    }

    @Override
    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDao.delete(CategoryMapper.toEntity(category)));
    }
}
