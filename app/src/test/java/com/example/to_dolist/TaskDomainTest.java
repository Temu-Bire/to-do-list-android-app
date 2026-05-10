package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Task;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Task domain model computed properties.
 */
public class TaskDomainTest {

    @Test
    public void isOverdue_completedTask_returnsFalse() {
        Task task = new Task(1, "T", "", Priority.HIGH,
                System.currentTimeMillis() - 86_400_000L, // yesterday
                true, // completed
                null, false, false, null);
        assertFalse(task.isOverdue());
    }

    @Test
    public void isOverdue_pastDueIncomplete_returnsTrue() {
        Task task = new Task(1, "T", "", Priority.HIGH,
                System.currentTimeMillis() - 86_400_000L, // yesterday
                false, null, false, false, null);
        assertTrue(task.isOverdue());
    }

    @Test
    public void isOverdue_futureTask_returnsFalse() {
        Task task = new Task(1, "T", "", Priority.HIGH,
                System.currentTimeMillis() + 86_400_000L, // tomorrow
                false, null, false, false, null);
        assertFalse(task.isOverdue());
    }

    @Test
    public void subtaskCompletedCount_correctCount() {
        Task task = new Task(1, "T", "", Priority.LOW,
                System.currentTimeMillis(), false, null, false, false, null);
        com.example.to_dolist.domain.model.Subtask s1 =
                new com.example.to_dolist.domain.model.Subtask(1, 1, "A", true);
        com.example.to_dolist.domain.model.Subtask s2 =
                new com.example.to_dolist.domain.model.Subtask(2, 1, "B", false);
        task.setSubtasks(java.util.Arrays.asList(s1, s2));
        assertEquals(1, task.getSubtaskCompletedCount());
    }
}
