package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Priority;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SuggestPriorityUseCase.
 * No mocking needed — this is a pure Java class with no Android dependencies.
 */
public class SuggestPriorityUseCaseTest {

    private final SuggestPriorityUseCase useCase = new SuggestPriorityUseCase();

    @Test
    public void urgentKeyword_returnsHigh() {
        Priority result = useCase.execute("Urgent report", "Submit ASAP");
        assertEquals(Priority.HIGH, result);
    }

    @Test
    public void lowKeyword_returnsLow() {
        Priority result = useCase.execute("Maybe clean desk", "someday eventually");
        assertEquals(Priority.LOW, result);
    }

    @Test
    public void noKeywords_returnsMedium() {
        Priority result = useCase.execute("Buy groceries", "Milk and eggs");
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    public void nullInputs_returnsMedium() {
        Priority result = useCase.execute(null, null);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    public void moreHighThanLow_returnsHigh() {
        Priority result = useCase.execute("Critical urgent deadline", "maybe later");
        assertEquals(Priority.HIGH, result);
    }
}
