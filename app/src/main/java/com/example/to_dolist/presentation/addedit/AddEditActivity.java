package com.example.to_dolist.presentation.addedit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.DateUtils;
import com.example.to_dolist.core.util.UiResources;
import com.example.to_dolist.databinding.ActivityAddEditBinding;
import com.example.to_dolist.presentation.BaseActivity;
import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.model.TaskWorkflowStatus;
import com.example.to_dolist.worker.ReminderWorker;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEditActivity extends BaseActivity {

    public static final String EXTRA_TASK_ID          = "extra_task_id";
    public static final String EXTRA_TASK_TITLE       = "extra_task_title";
    public static final String EXTRA_TASK_DESC        = "extra_task_desc";
    public static final String EXTRA_TASK_PRIORITY    = "extra_task_priority";
    public static final String EXTRA_TASK_DUE_DATE    = "extra_task_due_date";
    public static final String EXTRA_TASK_COMPLETED   = "extra_task_completed";
    public static final String EXTRA_TASK_REMINDER    = "extra_task_reminder";
    public static final String EXTRA_TASK_RECURRING   = "extra_task_recurring";
    public static final String EXTRA_TASK_CATEGORY_ID = "extra_task_category_id";
    public static final String EXTRA_TASK_WORKFLOW     = "extra_task_workflow";
    public static final String EXTRA_TASK_SORT_ORDER  = "extra_task_sort_order";

    private ActivityAddEditBinding binding;
    private AddEditViewModel viewModel;
    private SubtaskAdapter subtaskAdapter;
    private final List<Category> categoriesBuffer = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(AddEditViewModel.class);
        
        setupSubtaskList();
        setupPriorityChips();
        setupDatePicker();
        setupSaveButton();
        setupAutoSuggest();
        setupWorkflowChips();
        setupCategorySpinner();
        loadIntentData();
        observeViewModel();
    }

    private void setupWorkflowChips() {
        binding.chipWorkflowPending.setOnClickListener(v ->
                viewModel.setWorkflowStatus(TaskWorkflowStatus.PENDING));
        binding.chipWorkflowInProgress.setOnClickListener(v ->
                viewModel.setWorkflowStatus(TaskWorkflowStatus.IN_PROGRESS));
    }

    private void setupCategorySpinner() {
        Spinner spinner = binding.spinnerCategory;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > categoriesBuffer.size()) {
                    viewModel.setCategoryId(null);
                    return;
                }
                viewModel.setCategoryId(categoriesBuffer.get(position - 1).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCategoryId(null);
            }
        });
    }

    private void setupSubtaskList() {
        subtaskAdapter = new SubtaskAdapter(
                index -> viewModel.toggleSubtask(index),
                index -> viewModel.removeSubtask(index)
        );
        binding.recyclerSubtasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSubtasks.setAdapter(subtaskAdapter);

        binding.buttonAddSubtask.setOnClickListener(v -> {
            String text = binding.editSubtaskInput.getText() != null
                    ? binding.editSubtaskInput.getText().toString() : "";
            viewModel.addSubtask(text);
            binding.editSubtaskInput.setText("");
        });
    }

    private void setupPriorityChips() {
        binding.chipHigh.setOnClickListener(v   -> viewModel.setPriority(Priority.HIGH));
        binding.chipMedium.setOnClickListener(v -> viewModel.setPriority(Priority.MEDIUM));
        binding.chipLow.setOnClickListener(v    -> viewModel.setPriority(Priority.LOW));
    }

    private void setupDatePicker() {
        binding.buttonPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            Long current = viewModel.getDueDate().getValue();
            if (current != null) cal.setTimeInMillis(current);

            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day, 0, 0, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        viewModel.setDueDate(selected.getTimeInMillis());
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupSaveButton() {
        binding.fabSave.setOnClickListener(v -> viewModel.saveTask());
    }

    /** When user stops typing, request an AI priority suggestion. */
    private void setupAutoSuggest() {
        binding.editTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)    {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setTitle(s.toString());
                viewModel.requestPrioritySuggestion();
            }
        });
        binding.editDescription.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)    {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setDescription(s.toString());
                viewModel.requestPrioritySuggestion();
            }
        });

        binding.switchReminder.setOnCheckedChangeListener(
                (btn, checked) -> viewModel.setReminder(checked));
    }

    private void loadIntentData() {
        int taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        if (taskId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.edit_task);

            String wfName = getIntent().getStringExtra(EXTRA_TASK_WORKFLOW);
            TaskWorkflowStatus wf = wfName != null
                    ? TaskWorkflowStatus.fromKey(wfName)
                    : TaskWorkflowStatus.PENDING;
            int sortOrder = getIntent().getIntExtra(EXTRA_TASK_SORT_ORDER, 0);
            Task task = new Task(
                    taskId,
                    getIntent().getStringExtra(EXTRA_TASK_TITLE),
                    getIntent().getStringExtra(EXTRA_TASK_DESC),
                    Priority.fromLabel(getIntent().getStringExtra(EXTRA_TASK_PRIORITY)),
                    getIntent().getLongExtra(EXTRA_TASK_DUE_DATE, System.currentTimeMillis()),
                    getIntent().getBooleanExtra(EXTRA_TASK_COMPLETED, false),
                    getIntent().getIntExtra(EXTRA_TASK_CATEGORY_ID, -1) == -1
                            ? null : getIntent().getIntExtra(EXTRA_TASK_CATEGORY_ID, -1),
                    getIntent().getBooleanExtra(EXTRA_TASK_REMINDER, false),
                    getIntent().getBooleanExtra(EXTRA_TASK_RECURRING, false),
                    null,
                    wf,
                    sortOrder
            );
            viewModel.loadTask(task);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.add_task);
        }
    }

    private void observeViewModel() {
        // Sync title/description to EditText only once (avoid loop)
        viewModel.getTitle().observe(this, t -> {
            if (!binding.editTitle.getText().toString().equals(t)) {
                binding.editTitle.setText(t);
            }
        });

        // Due date label
        viewModel.getDueDate().observe(this, date ->
                binding.textDueDateValue.setText(date != null
                        ? DateUtils.toFriendlyLabel(this, date)
                        : ""));

        // Priority chip selection
        viewModel.getPriority().observe(this, p -> {
            binding.chipHigh.setChecked(p == Priority.HIGH);
            binding.chipMedium.setChecked(p == Priority.MEDIUM);
            binding.chipLow.setChecked(p == Priority.LOW);
        });

        // AI suggestion banner
        viewModel.getSuggestedPriority().observe(this, suggested -> {
            if (suggested != null && suggested != viewModel.getPriority().getValue()) {
                binding.textPrioritySuggestion.setVisibility(View.VISIBLE);
                binding.textPrioritySuggestion.setText(
                        getString(R.string.priority_suggestion,
                                UiResources.priorityLabel(this, suggested)));
                binding.textPrioritySuggestion.setOnClickListener(v -> {
                    viewModel.setPriority(suggested);
                    binding.textPrioritySuggestion.setVisibility(View.GONE);
                });
            } else {
                binding.textPrioritySuggestion.setVisibility(View.GONE);
            }
        });

        // Subtasks
        viewModel.getSubtasks().observe(this, list -> subtaskAdapter.submitList(list));

        // Reminder toggle
        viewModel.getReminder().observe(this, enabled ->
                binding.switchReminder.setChecked(enabled));

        viewModel.getWorkflowStatus().observe(this, ws -> {
            if (ws == null) return;
            binding.chipGroupWorkflow.check(
                    ws == TaskWorkflowStatus.IN_PROGRESS
                            ? binding.chipWorkflowInProgress.getId()
                            : binding.chipWorkflowPending.getId());
        });

        viewModel.getCategories().observe(this, categories -> {
            categoriesBuffer.clear();
            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.category_all));
            if (categories != null) {
                categoriesBuffer.addAll(categories);
                for (Category c : categories) {
                    labels.add(c.getName() != null ? c.getName() : "");
                }
            }
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> spAdapter =
                    (ArrayAdapter<String>) binding.spinnerCategory.getAdapter();
            spAdapter.clear();
            spAdapter.addAll(labels);
            spAdapter.notifyDataSetChanged();

            Integer selected = viewModel.getCategoryId().getValue();
            if (selected != null) {
                for (int i = 0; i < categoriesBuffer.size(); i++) {
                    if (categoriesBuffer.get(i).getId() == selected) {
                        binding.spinnerCategory.setSelection(i + 1, false);
                        break;
                    }
                }
            }
        });

        // Save success
        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                // Schedule/cancel reminder via WorkManager
                Long dueDate = viewModel.getDueDate().getValue();
                Boolean reminder = viewModel.getReminder().getValue();
                int taskId = viewModel.getEditingTaskId();
                String title = viewModel.getTitle().getValue();

                if (reminder != null && reminder && dueDate != null && taskId != -1 && title != null) {
                    ReminderWorker.schedule(this, taskId, title, dueDate);
                } else if (taskId != -1) {
                    ReminderWorker.cancel(this, taskId);
                }
                finish();
            }
        });

        // Errors
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
