package com.example.to_dolist.domain.usecase;

import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.to_dolist.domain.repository.ITaskRepository;

public class AddTaskUseCaseTest {

    @Mock
    private ITaskRepository mockRepository;

    private AddTaskUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new AddTaskUseCase(mockRepository);
    }

    private Task validTask(String title) {
        return new Task(0, title, "desc", Priority.MEDIUM,
                System.currentTimeMillis(), false, null, false, false, null);
    }

    @Test
    public void validTask_insertsToRepository() {
        Task task = validTask("Buy groceries");
        useCase.execute(task);
        verify(mockRepository, times(1)).insertTask(task);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyTitle_throwsException() {
        useCase.execute(validTask(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankTitle_throwsException() {
        useCase.execute(validTask("   "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullTitle_throwsException() {
        useCase.execute(new Task(0, null, "", Priority.LOW,
                System.currentTimeMillis(), false, null, false, false, null));
    }

    @Test
    public void validTask_repositoryCalledExactlyOnce() {
        Task task = validTask("Finish report");
        useCase.execute(task);
        verify(mockRepository, times(1)).insertTask(any());
        verifyNoMoreInteractions(mockRepository);
    }
}
