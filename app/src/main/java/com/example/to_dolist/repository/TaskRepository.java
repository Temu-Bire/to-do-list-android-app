package com.example.to_dolist.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.to_dolist.data.Task;
import com.example.to_dolist.data.TaskDao;
import com.example.to_dolist.data.TaskDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<List<Task>> sortedByPriority;
    private final LiveData<List<Task>> sortedByDate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        completedTasks = taskDao.getCompletedTasks();
        sortedByPriority = taskDao.getAllTasksSortedByPriority();
        sortedByDate = taskDao.getAllTasksSortedByDate();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }

    public LiveData<List<Task>> getSortedByPriority() {
        return sortedByPriority;
    }

    public LiveData<List<Task>> getSortedByDate() {
        return sortedByDate;
    }

    public void insert(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }
}
