package com.example.to_dolist.data.mapper;

import com.example.to_dolist.data.local.entity.SubtaskEntity;
import com.example.to_dolist.domain.model.Subtask;

public class SubtaskMapper {

    public static Subtask toDomain(SubtaskEntity entity) {
        return new Subtask(entity.getId(), entity.getTaskId(),
                entity.getTitle(), entity.isCompleted());
    }

    public static SubtaskEntity toEntity(Subtask subtask) {
        SubtaskEntity entity = new SubtaskEntity(
                subtask.getTaskId(), subtask.getTitle(), subtask.isCompleted());
        if (subtask.getId() != 0) entity.setId(subtask.getId());
        return entity;
    }
}
