package com.example.to_dolist.presentation.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.model.TaskWorkflowStatus;
import com.example.to_dolist.domain.usecase.AddTaskUseCase;
import com.example.to_dolist.domain.usecase.DeleteTaskUseCase;
import com.example.to_dolist.domain.usecase.GetAllTasksUseCase;
import com.example.to_dolist.domain.usecase.GetCategoriesUseCase;
import com.example.to_dolist.domain.usecase.GetOverdueTasksUseCase;
import com.example.to_dolist.domain.usecase.UpdateTaskUseCase;
import com.example.to_dolist.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    public enum FilterMode { ALL, ACTIVE, COMPLETED, OVERDUE, IN_PROGRESS }
    public enum SortMode { BY_DATE, BY_PRIORITY }

    private final GetAllTasksUseCase getAllTasksUseCase;
    private final AddTaskUseCase addTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetOverdueTasksUseCase getOverdueTasksUseCase;
    private final ITaskRepository taskRepository;

    private final MutableLiveData<FilterMode> filterMode = new MutableLiveData<>(FilterMode.ALL);
    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.BY_DATE);
    private final MutableLiveData<Integer> categoryId = new MutableLiveData<>(null);

    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> baseTasks;

    private final MediatorLiveData<List<Task>> tasks = new MediatorLiveData<>();
    private List<Task> lastBase = new ArrayList<>();

    private final LiveData<List<Task>> overdueTasks;
    private final LiveData<List<Category>> categories;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public HomeViewModel(GetAllTasksUseCase getAllTasksUseCase,
                         AddTaskUseCase addTaskUseCase,
                         UpdateTaskUseCase updateTaskUseCase,
                         DeleteTaskUseCase deleteTaskUseCase,
                         GetCategoriesUseCase getCategoriesUseCase,
                         GetOverdueTasksUseCase getOverdueTasksUseCase,
                         ITaskRepository taskRepository) {

        this.getAllTasksUseCase = getAllTasksUseCase;
        this.addTaskUseCase = addTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.getOverdueTasksUseCase = getOverdueTasksUseCase;
        this.taskRepository = taskRepository;

        allTasks = getAllTasksUseCase.execute();
        baseTasks = Transformations.switchMap(categoryId, cid ->
                cid == null ? allTasks : getAllTasksUseCase.executeByCategory(cid));

        overdueTasks = getOverdueTasksUseCase.execute();
        categories = getCategoriesUseCase.execute();

        tasks.addSource(baseTasks, this::onBaseChanged);
        tasks.addSource(filterMode, f -> recomputeTasks());
        tasks.addSource(sortMode, s -> recomputeTasks());
    }

    private void onBaseChanged(List<Task> list) {
        lastBase = list != null ? new ArrayList<>(list) : new ArrayList<>();
        recomputeTasks();
    }

    private void recomputeTasks() {
        FilterMode filter = filterMode.getValue();
        if (filter == null) filter = FilterMode.ALL;

        List<Task> step = new ArrayList<>(lastBase);
        switch (filter) {
            case ACTIVE:
                step.removeIf(Task::isCompleted);
                break;
            case COMPLETED:
                step.removeIf(t -> !t.isCompleted());
                break;
            case OVERDUE:
                step.removeIf(t -> !t.isOverdue());
                break;
            case IN_PROGRESS:
                step.removeIf(t -> t.isCompleted()
                        || t.getWorkflowStatus() != TaskWorkflowStatus.IN_PROGRESS);
                break;
            case ALL:
            default:
                break;
        }

        SortMode sort = sortMode.getValue();
        if (sort == SortMode.BY_PRIORITY) {
            step.sort(Comparator
                    .comparingInt((Task t) -> t.getPriority().ordinal())
                    .thenComparingLong(Task::getDueDate));
        } else {
            step.sort(Comparator
                    .comparingInt(Task::getSortOrder)
                    .thenComparingLong(Task::getDueDate));
        }

        tasks.setValue(step);
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<List<Task>> getOverdueTasks() {
        return overdueTasks;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setFilter(FilterMode mode) {
        filterMode.setValue(mode);
    }

    public void setSort(SortMode mode) {
        sortMode.setValue(mode);
    }

    /** Pass null to clear category filter. */
    public void setCategoryFilter(Integer catId) {
        categoryId.setValue(catId);
    }

    public FilterMode getCurrentFilter() {
        return filterMode.getValue();
    }

    public SortMode getCurrentSort() {
        return sortMode.getValue();
    }

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

    public void persistTaskOrder(List<Task> ordered) {
        taskRepository.updateSortOrders(ordered);
    }

    public void toggleComplete(Task task) {
        task.setCompleted(!task.isCompleted());
        if (task.isCompleted()) {
            task.setWorkflowStatus(TaskWorkflowStatus.PENDING);
        }
        updateTaskUseCase.execute(task);
    }
}
