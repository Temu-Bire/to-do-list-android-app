package com.example.to_dolist.presentation.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.usecase.AddTaskUseCase;
import com.example.to_dolist.domain.usecase.DeleteTaskUseCase;
import com.example.to_dolist.domain.usecase.SearchTasksUseCase;
import com.example.to_dolist.domain.usecase.UpdateTaskUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final SearchTasksUseCase searchTasksUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final AddTaskUseCase addTaskUseCase;

    private final MutableLiveData<String> queryLiveData = new MutableLiveData<>("");

    private final LiveData<List<Task>> searchResults;

    @Inject
    public SearchViewModel(SearchTasksUseCase searchTasksUseCase,
                           DeleteTaskUseCase deleteTaskUseCase,
                           UpdateTaskUseCase updateTaskUseCase,
                           AddTaskUseCase addTaskUseCase) {
        this.searchTasksUseCase = searchTasksUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.addTaskUseCase = addTaskUseCase;
        this.searchResults = Transformations.switchMap(queryLiveData, searchTasksUseCase::execute);
    }

    public void setQuery(String query) {
        queryLiveData.setValue(query == null ? "" : query);
    }

    public String getCurrentQuery() {
        return queryLiveData.getValue();
    }

    public LiveData<List<Task>> getSearchResults() {
        return searchResults;
    }

    public void delete(Task task) {
        deleteTaskUseCase.execute(task);
    }

    public void update(Task task) {
        updateTaskUseCase.execute(task);
    }

    /** Restores a deleted task when the user taps Undo (same semantics as Home). */
    public void restore(Task task) {
        try {
            addTaskUseCase.execute(task);
        } catch (IllegalArgumentException ignored) {
            /* title missing — should not occur for restored deletes */
        }
    }
}
