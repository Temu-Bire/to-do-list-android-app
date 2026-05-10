package com.example.to_dolist.presentation.addedit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Subtask;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.usecase.AddTaskUseCase;
import com.example.to_dolist.domain.usecase.GetCategoriesUseCase;
import com.example.to_dolist.domain.usecase.SuggestPriorityUseCase;
import com.example.to_dolist.domain.usecase.UpdateTaskUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddEditViewModel extends ViewModel {

    private final AddTaskUseCase addTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final SuggestPriorityUseCase suggestPriorityUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;

    // ─── Form state ───────────────────────────────────────────────────────────
    private final MutableLiveData<String>        title        = new MutableLiveData<>("");
    private final MutableLiveData<String>        description  = new MutableLiveData<>("");
    private final MutableLiveData<Priority>      priority     = new MutableLiveData<>(Priority.MEDIUM);
    private final MutableLiveData<Long>          dueDate      = new MutableLiveData<>(System.currentTimeMillis());
    private final MutableLiveData<Boolean>       completed    = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean>       reminder     = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean>       recurring    = new MutableLiveData<>(false);
    private final MutableLiveData<String>        recurrenceRule = new MutableLiveData<>("NONE");
    private final MutableLiveData<Integer>       categoryId   = new MutableLiveData<>(null);
    private final MutableLiveData<List<Subtask>> subtasks     = new MutableLiveData<>(new ArrayList<>());

    // ─── UI events ────────────────────────────────────────────────────────────
    private final MutableLiveData<Boolean>  saveSuccess  = new MutableLiveData<>(false);
    private final MutableLiveData<String>   errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Priority> suggestedPriority = new MutableLiveData<>();

    private int editingTaskId = -1; // -1 = new task

    @Inject
    public AddEditViewModel(AddTaskUseCase addTaskUseCase,
                            UpdateTaskUseCase updateTaskUseCase,
                            SuggestPriorityUseCase suggestPriorityUseCase,
                            GetCategoriesUseCase getCategoriesUseCase) {
        this.addTaskUseCase       = addTaskUseCase;
        this.updateTaskUseCase    = updateTaskUseCase;
        this.suggestPriorityUseCase = suggestPriorityUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
    }

    /** Populate the form when editing an existing task. */
    public void loadTask(Task task) {
        editingTaskId = task.getId();
        title.setValue(task.getTitle());
        description.setValue(task.getDescription());
        priority.setValue(task.getPriority());
        dueDate.setValue(task.getDueDate());
        completed.setValue(task.isCompleted());
        reminder.setValue(task.isReminderEnabled());
        recurring.setValue(task.isRecurring());
        recurrenceRule.setValue(task.getRecurrenceRule());
        categoryId.setValue(task.getCategoryId());
        subtasks.setValue(task.getSubtasks() != null ? new ArrayList<>(task.getSubtasks()) : new ArrayList<>());
    }

    /** Called when the user pauses typing to get a priority suggestion. */
    public void requestPrioritySuggestion() {
        String t = title.getValue();
        String d = description.getValue();
        Priority suggested = suggestPriorityUseCase.execute(t, d);
        suggestedPriority.setValue(suggested);
    }

    public void addSubtask(String subtaskTitle) {
        if (subtaskTitle == null || subtaskTitle.trim().isEmpty()) return;
        List<Subtask> current = subtasks.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(new Subtask(0, editingTaskId == -1 ? 0 : editingTaskId, subtaskTitle.trim(), false));
        subtasks.setValue(current);
    }

    public void removeSubtask(int index) {
        List<Subtask> current = subtasks.getValue();
        if (current != null && index >= 0 && index < current.size()) {
            current.remove(index);
            subtasks.setValue(current);
        }
    }

    public void toggleSubtask(int index) {
        List<Subtask> current = subtasks.getValue();
        if (current != null && index >= 0 && index < current.size()) {
            Subtask s = current.get(index);
            s.setCompleted(!s.isCompleted());
            subtasks.setValue(current);
        }
    }

    public void saveTask() {
        String t = title.getValue();
        if (t == null || t.trim().isEmpty()) {
            errorMessage.setValue("Title is required.");
            return;
        }

        Task task = new Task(
                editingTaskId == -1 ? 0 : editingTaskId,
                t.trim(),
                description.getValue() != null ? description.getValue().trim() : "",
                priority.getValue() != null ? priority.getValue() : Priority.MEDIUM,
                dueDate.getValue() != null ? dueDate.getValue() : System.currentTimeMillis(),
                completed.getValue() != null && completed.getValue(),
                categoryId.getValue(),
                reminder.getValue() != null && reminder.getValue(),
                recurring.getValue() != null && recurring.getValue(),
                recurrenceRule.getValue()
        );
        task.setSubtasks(subtasks.getValue());

        try {
            if (editingTaskId == -1) {
                addTaskUseCase.execute(task);
            } else {
                updateTaskUseCase.execute(task);
            }
            saveSuccess.setValue(true);
        } catch (IllegalArgumentException e) {
            errorMessage.setValue(e.getMessage());
        }
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public boolean isEditing()                        { return editingTaskId != -1; }
    public int getEditingTaskId()                     { return editingTaskId; }

    public LiveData<String>        getTitle()         { return title; }
    public LiveData<String>        getDescription()   { return description; }
    public LiveData<Priority>      getPriority()      { return priority; }
    public LiveData<Long>          getDueDate()       { return dueDate; }
    public LiveData<Boolean>       getCompleted()     { return completed; }
    public LiveData<Boolean>       getReminder()      { return reminder; }
    public LiveData<Boolean>       getRecurring()     { return recurring; }
    public LiveData<Integer>       getCategoryId()    { return categoryId; }
    public LiveData<List<Subtask>> getSubtasks()      { return subtasks; }
    public LiveData<Boolean>       getSaveSuccess()   { return saveSuccess; }
    public LiveData<String>        getErrorMessage()  { return errorMessage; }
    public LiveData<Priority>      getSuggestedPriority() { return suggestedPriority; }
    public LiveData<List<Category>> getCategories()  { return getCategoriesUseCase.execute(); }

    public void setTitle(String v)          { title.setValue(v); }
    public void setDescription(String v)    { description.setValue(v); }
    public void setPriority(Priority v)     { priority.setValue(v); }
    public void setDueDate(long v)          { dueDate.setValue(v); }
    public void setReminder(boolean v)      { reminder.setValue(v); }
    public void setRecurring(boolean v)     { recurring.setValue(v); }
    public void setRecurrenceRule(String v) { recurrenceRule.setValue(v); }
    public void setCategoryId(Integer v)    { categoryId.setValue(v); }
}
