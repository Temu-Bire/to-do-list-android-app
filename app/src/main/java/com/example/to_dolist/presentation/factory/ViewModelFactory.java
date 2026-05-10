package com.example.to_dolist.presentation.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.to_dolist.domain.repository.ITaskRepository;
import com.example.to_dolist.domain.usecase.AddTaskUseCase;
import com.example.to_dolist.domain.usecase.DeleteTaskUseCase;
import com.example.to_dolist.domain.usecase.GetAllTasksUseCase;
import com.example.to_dolist.domain.usecase.GetCategoriesUseCase;
import com.example.to_dolist.domain.usecase.GetOverdueTasksUseCase;
import com.example.to_dolist.domain.usecase.SearchTasksUseCase;
import com.example.to_dolist.domain.usecase.SuggestPriorityUseCase;
import com.example.to_dolist.domain.usecase.UpdateTaskUseCase;
import com.example.to_dolist.presentation.addedit.AddEditViewModel;
import com.example.to_dolist.presentation.home.HomeViewModel;
import com.example.to_dolist.presentation.search.SearchViewModel;

/**
 * Manual DI ViewModelFactory.
 *
 * Senior engineer note: This is the standard Java alternative to Hilt's @HiltViewModel.
 * It constructs ViewModels with their exact dependencies injected — fully testable.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final ITaskRepository repository;

    public ViewModelFactory(ITaskRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    new GetAllTasksUseCase(repository),
                    new AddTaskUseCase(repository),
                    new UpdateTaskUseCase(repository),
                    new DeleteTaskUseCase(repository),
                    new GetCategoriesUseCase(repository),
                    new GetOverdueTasksUseCase(repository),
                    repository
            );
        }

        if (modelClass.isAssignableFrom(AddEditViewModel.class)) {
            return (T) new AddEditViewModel(
                    new AddTaskUseCase(repository),
                    new UpdateTaskUseCase(repository),
                    new SuggestPriorityUseCase(),
                    new GetCategoriesUseCase(repository)
            );
        }

        if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(
                    new SearchTasksUseCase(repository),
                    new DeleteTaskUseCase(repository),
                    new UpdateTaskUseCase(repository),
                    new AddTaskUseCase(repository)
            );
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
