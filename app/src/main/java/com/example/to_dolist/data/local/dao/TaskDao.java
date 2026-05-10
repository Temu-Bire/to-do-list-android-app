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

    // All tasks sorted by due date
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> getAllTasks();

    // Only completed tasks
    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> getCompletedTasks();

    // Smart priority sort: High → Medium → Low, then by date
    @Query("SELECT * FROM tasks ORDER BY " +
           "CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, " +
           "dueDate ASC")
    LiveData<List<TaskEntity>> getTasksSortedByPriority();

    // Filter by category
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> getTasksByCategory(int categoryId);

    // Real-time search — title OR description
    @Query("SELECT * FROM tasks WHERE " +
           "title LIKE '%' || :query || '%' OR " +
           "description LIKE '%' || :query || '%' " +
           "ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> searchTasks(String query);

    // Overdue = not completed AND due date is in the past
    @Query("SELECT * FROM tasks WHERE completed = 0 AND dueDate < :now ORDER BY dueDate ASC")
    LiveData<List<TaskEntity>> getOverdueTasks(long now);
}
