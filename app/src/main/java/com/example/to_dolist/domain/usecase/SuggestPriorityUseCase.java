package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Priority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * On-device AI-like priority suggestion based on keyword matching.
 *
 * Senior engineer note: This is a rule-based heuristic — not ML.
 * It runs instantly, works offline, and is fully testable without mocking.
 * A Gemini API call could replace this in Phase 2 of advanced features.
 */
public class SuggestPriorityUseCase {

    private static final Set<String> HIGH_KEYWORDS = new HashSet<>(Arrays.asList(
            "urgent", "asap", "critical", "emergency", "immediately",
            "deadline", "important", "must", "high", "priority"
    ));

    private static final Set<String> LOW_KEYWORDS = new HashSet<>(Arrays.asList(
            "maybe", "someday", "eventually", "low", "later",
            "whenever", "optional", "if", "possible", "backlog"
    ));

    @Inject
    public SuggestPriorityUseCase() {
    }

    /**
     * Analyzes the task title and description to suggest a priority level.
     *
     * @param title       task title
     * @param description task description
     * @return suggested Priority
     */
    public Priority execute(String title, String description) {
        String combined = ((title == null ? "" : title) + " " +
                (description == null ? "" : description)).toLowerCase();

        String[] words = combined.split("\\W+");

        int highScore = 0;
        int lowScore = 0;

        for (String word : words) {
            if (HIGH_KEYWORDS.contains(word)) highScore++;
            if (LOW_KEYWORDS.contains(word)) lowScore++;
        }

        if (highScore > 0 && highScore >= lowScore) return Priority.HIGH;
        if (lowScore > 0 && lowScore > highScore) return Priority.LOW;
        return Priority.MEDIUM;
    }
}
