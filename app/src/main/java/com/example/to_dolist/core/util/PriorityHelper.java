package com.example.to_dolist.core.util;

import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Smart sorting utilities — implements the Eisenhower Matrix logic:
 *
 *   Quadrant 1 (Do First):  HIGH priority + overdue     → score 0
 *   Quadrant 2 (Schedule):  HIGH priority + not overdue → score 1
 *   Quadrant 3 (Delegate):  MEDIUM + overdue            → score 2
 *   Quadrant 4 (Eliminate): LOW + not overdue           → score 3+
 */
public class PriorityHelper {

    private PriorityHelper() {} // utility class — no instances

    /**
     * Returns a new list sorted by smart (Eisenhower) priority.
     * Does NOT modify the original list.
     */
    public static List<Task> smartSort(List<Task> tasks) {
        if (tasks == null) return new ArrayList<>();
        List<Task> sorted = new ArrayList<>(tasks);
        Collections.sort(sorted, SMART_COMPARATOR);
        return sorted;
    }

    /**
     * Assigns a numeric urgency score to a task.
     * Lower score = higher urgency.
     */
    public static int urgencyScore(Task task) {
        boolean overdue = task.isOverdue();
        Priority p = task.getPriority();

        if (p == Priority.HIGH   && overdue)  return 0;  // Critical — do immediately
        if (p == Priority.HIGH   && !overdue) return 1;  // Important — schedule soon
        if (p == Priority.MEDIUM && overdue)  return 2;  // Delegate or address quickly
        if (p == Priority.MEDIUM && !overdue) return 3;  // Normal
        if (p == Priority.LOW    && overdue)  return 4;  // Low but past due
        return 5;                                          // Low, not urgent
    }

    private static final Comparator<Task> SMART_COMPARATOR = (a, b) -> {
        int scoreA = urgencyScore(a);
        int scoreB = urgencyScore(b);
        if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);
        // Tie-break by due date (earlier first)
        return Long.compare(a.getDueDate(), b.getDueDate());
    };
}
