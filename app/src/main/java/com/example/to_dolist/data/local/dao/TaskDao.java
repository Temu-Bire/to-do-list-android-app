package com.example.to_dolist.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.to_dolist.data.local.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity findByIdSync(int id);

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM tasks")
    int peekNextSortOrder();

    @Query("UPDATE tasks SET sortOrder = :sortOrder WHERE id = :id")
    void updateSortOrder(int id, int sortOrder);

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getIncompleteTasks();

    @Query("SELECT * FROM tasks WHERE completed = 0 AND workflowStatus = 'IN_PROGRESS' " +
           "ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getInProgressTasks();

    @Query("SELECT * FROM tasks ORDER BY " +
           "CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, " +
           "sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getTasksSortedByPriority();

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> getTasksByCategory(int categoryId);

    @Query("SELECT * FROM tasks WHERE " +
           "title LIKE '%' || :query || '%' OR " +
           "description LIKE '%' || :query || '%' " +
           "ORDER BY sortOrder ASC, dueDate ASC")
    LiveData<List<TaskEntity>> searchTasks(String query);

    @Query("SELECT * FROM tasks WHERE completed = 0 AND dueDate < :now " +
           "ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> getOverdueTasks(long now);

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, id ASC")
    List<TaskEntity> getAllTasksSnapshot();
}
