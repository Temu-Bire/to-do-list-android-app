package com.example.to_dolist.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.to_dolist.data.local.entity.SubtaskEntity;

import java.util.List;

@Dao
public interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubtaskEntity subtask);

    @Update
    void update(SubtaskEntity subtask);

    @Delete
    void delete(SubtaskEntity subtask);

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC")
    LiveData<List<SubtaskEntity>> getSubtasksForTask(int taskId);

    // Used by mapper to eagerly load subtasks (synchronous)
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC")
    List<SubtaskEntity> getSubtasksForTaskSync(int taskId);

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    void deleteAllForTask(int taskId);
}
