package com.example.to_dolist.core.util;

import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Task;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PriorityHelperTest {

    private Task makeTask(Priority p, boolean overdue) {
        long dueDate = overdue
                ? System.currentTimeMillis() - 86_400_000L  // yesterday
                : System.currentTimeMillis() + 86_400_000L; // tomorrow
        return new Task(0, "Task", "", p, dueDate, false, null, false, false, null);
    }

    @Test
    public void highOverdue_ranksFirst() {
        Task high   = makeTask(Priority.HIGH, true);
        Task low    = makeTask(Priority.LOW, false);
        Task medium = makeTask(Priority.MEDIUM, false);

        List<Task> sorted = PriorityHelper.smartSort(Arrays.asList(low, medium, high));
        assertEquals(Priority.HIGH, sorted.get(0).getPriority());
    }

    @Test
    public void lowNotOverdue_ranksLast() {
        Task high   = makeTask(Priority.HIGH, false);
        Task low    = makeTask(Priority.LOW, false);
        Task medium = makeTask(Priority.MEDIUM, false);

        List<Task> sorted = PriorityHelper.smartSort(Arrays.asList(low, medium, high));
        assertEquals(Priority.LOW, sorted.get(sorted.size() - 1).getPriority());
    }

    @Test
    public void urgencyScore_highOverdue_isZero() {
        assertEquals(0, PriorityHelper.urgencyScore(makeTask(Priority.HIGH, true)));
    }

    @Test
    public void urgencyScore_lowNotOverdue_isHighest() {
        int score = PriorityHelper.urgencyScore(makeTask(Priority.LOW, false));
        assertTrue(score >= 5);
    }

    @Test
    public void nullList_returnsEmptyList() {
        List<Task> result = PriorityHelper.smartSort(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void originalList_notMutated() {
        Task a = makeTask(Priority.LOW, false);
        Task b = makeTask(Priority.HIGH, true);
        List<Task> original = Arrays.asList(a, b);
        PriorityHelper.smartSort(original);
        // Original order unchanged
        assertEquals(Priority.LOW, original.get(0).getPriority());
    }
}
