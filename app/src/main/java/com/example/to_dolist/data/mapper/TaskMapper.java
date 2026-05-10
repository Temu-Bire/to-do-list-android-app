package com.example.to_dolist.data.mapper;

import com.example.to_dolist.data.local.dao.SubtaskDao;
import com.example.to_dolist.data.local.entity.SubtaskEntity;
import com.example.to_dolist.data.local.entity.TaskEntity;
import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Subtask;
import com.example.to_dolist.domain.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskMapper {

    /**
     * Converts a TaskEntity to a domain Task.
     * Eagerly loads subtasks via a synchronous DAO call.
     * This must be called from a background thread.
     */
    public static Task toDomain(TaskEntity entity, SubtaskDao subtaskDao) {
        Task task = new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                Priority.fromLabel(entity.getPriority()),
                entity.getDueDate(),
                entity.isCompleted(),
                entity.getCategoryId(),
                entity.isReminderEnabled(),
                entity.isRecurring(),
                entity.getRecurrenceRule()
        );

        // Load subtasks eagerly
        List<SubtaskEntity> subtaskEntities = subtaskDao.getSubtasksForTaskSync(entity.getId());
        List<Subtask> subtasks = new ArrayList<>();
        for (SubtaskEntity se : subtaskEntities) {
            subtasks.add(SubtaskMapper.toDomain(se));
        }
        task.setSubtasks(subtasks);
        return task;
    }

    /**
     * Lightweight mapping without subtasks — used when subtasks are not needed (e.g. list view).
     */
    public static Task toDomainShallow(TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                Priority.fromLabel(entity.getPriority()),
                entity.getDueDate(),
                entity.isCompleted(),
                entity.getCategoryId(),
                entity.isReminderEnabled(),
                entity.isRecurring(),
                entity.getRecurrenceRule()
        );
    }

    public static TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity(
                task.getTitle(),
                task.getDescription(),
                task.getPriority().getLabel(),
                task.getDueDate(),
                task.isCompleted(),
                task.getCategoryId(),
                task.isReminderEnabled(),
                task.isRecurring(),
                task.getRecurrenceRule()
        );
        if (task.getId() != 0) entity.setId(task.getId());
        return entity;
    }

    /** Converts a list of entities (shallow, no subtasks). */
    public static List<Task> toDomainList(List<TaskEntity> entities) {
        List<Task> result = new ArrayList<>();
        for (TaskEntity entity : entities) {
            result.add(toDomainShallow(entity));
        }
        return result;
    }
}
