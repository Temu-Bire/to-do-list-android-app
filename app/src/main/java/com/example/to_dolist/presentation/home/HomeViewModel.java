package com.example.to_dolist.presentation.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.usecase.AddTaskUseCase;
import com.example.to_dolist.domain.usecase.DeleteTaskUseCase;
import com.example.to_dolist.domain.usecase.GetAllTasksUseCase;
import com.example.to_dolist.domain.usecase.GetCategoriesUseCase;
import com.example.to_dolist.domain.usecase.GetOverdueTasksUseCase;
import com.example.to_dolist.domain.usecase.UpdateTaskUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * HomeViewModel owns the UI state for the task list screen.
 *
 * Senior engineer note: ViewModel survives configuration changes (rotation).
 * It never holds a reference to a View or Context.
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    // ─── Use Cases (injected via Hilt) ────────────────────────────
    private final GetAllTasksUseCase getAllTasksUseCase;
    private final AddTaskUseCase addTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetOverdueTasksUseCase getOverdueTasksUseCase;

    // ─── Filter / Sort state ──────────────────────────────────────────────────
    public enum FilterMode { ALL, COMPLETED, BY_CATEGORY }
    public enum SortMode  { BY_DATE, BY_PRIORITY }

    private final MutableLiveData<FilterMode> filterMode = new MutableLiveData<>(FilterMode.ALL);
    private final MutableLiveData<SortMode>   sortMode   = new MutableLiveData<>(SortMode.BY_DATE);
    private final MutableLiveData<Integer>    categoryId = new MutableLiveData<>(null);

    // ─── Tasks LiveData ───────────────────────────────────────────────────────
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<List<Task>> prioritySortedTasks;
    private final LiveData<List<Task>> overdueTasks;

    /**
     * MediatorLiveData reacts to filter/sort changes and switches source automatically.
     */
    private final MediatorLiveData<List<Task>> tasks = new MediatorLiveData<>();

    // ─── Categories ───────────────────────────────────────────────────────────
    private final LiveData<List<Category>> categories;

    // ─── Error / snackbar message ─────────────────────────────────────────────
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public HomeViewModel(GetAllTasksUseCase getAllTasksUseCase,
                         AddTaskUseCase addTaskUseCase,
                         UpdateTaskUseCase updateTaskUseCase,
                         DeleteTaskUseCase deleteTaskUseCase,
                         GetCategoriesUseCase getCategoriesUseCase,
                         GetOverdueTasksUseCase getOverdueTasksUseCase) {

        this.getAllTasksUseCase    = getAllTasksUseCase;
        this.addTaskUseCase       = addTaskUseCase;
        this.updateTaskUseCase    = updateTaskUseCase;
        this.deleteTaskUseCase    = deleteTaskUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.getOverdueTasksUseCase = getOverdueTasksUseCase;

        allTasks          = getAllTasksUseCase.execute();
        completedTasks    = getAllTasksUseCase.executeCompleted();
        prioritySortedTasks = getAllTasksUseCase.executeSortedByPriority();
        overdueTasks      = getOverdueTasksUseCase.execute();
        categories        = getCategoriesUseCase.execute();

        // Whenever filterMode or sortMode changes, switch the active source
        tasks.addSource(filterMode, f -> updateTaskSource());
        tasks.addSource(sortMode,   s -> updateTaskSource());
        tasks.addSource(categoryId, c -> updateTaskSource());

        // Seed initial source
        updateTaskSource();
    }

    private LiveData<List<Task>> currentSource = null;

    private void updateTaskSource() {
        FilterMode filter = filterMode.getValue();
        SortMode   sort   = sortMode.getValue();
        Integer    catId  = categoryId.getValue();

        LiveData<List<Task>> newSource;

        if (filter == FilterMode.COMPLETED) {
            newSource = completedTasks;
        } else if (filter == FilterMode.BY_CATEGORY && catId != null) {
            newSource = getAllTasksUseCase.executeByCategory(catId);
        } else if (sort == SortMode.BY_PRIORITY) {
            newSource = prioritySortedTasks;
        } else {
            newSource = allTasks;
        }

        if (currentSource != null) tasks.removeSource(currentSource);
        currentSource = newSource;
        tasks.addSource(currentSource, tasks::setValue);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public LiveData<List<Task>> getTasks()        { return tasks; }
    public LiveData<List<Task>> getOverdueTasks() { return overdueTasks; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<String> getErrorMessage()     { return errorMessage; }

    public void setFilter(FilterMode mode)    { filterMode.setValue(mode); }
    public void setSort(SortMode mode)        { sortMode.setValue(mode); }
    public void setCategory(Integer catId)    { categoryId.setValue(catId); }

    public FilterMode getCurrentFilter()      { return filterMode.getValue(); }
    public SortMode   getCurrentSort()        { return sortMode.getValue(); }

    public void insert(Task task) {
        try {
            addTaskUseCase.execute(task);
        } catch (IllegalArgumentException e) {
            errorMessage.setValue(e.getMessage());
        }
    }

    public void update(Task task) {
        try {
            updateTaskUseCase.execute(task);
        } catch (IllegalArgumentException e) {
            errorMessage.setValue(e.getMessage());
        }
    }

    public void delete(Task task) {
        deleteTaskUseCase.execute(task);
    }

    public void toggleComplete(Task task) {
        task.setCompleted(!task.isCompleted());
        updateTaskUseCase.execute(task);
    }
}
