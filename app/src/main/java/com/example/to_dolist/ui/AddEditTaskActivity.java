package com.example.to_dolist.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.to_dolist.R;
import com.example.to_dolist.data.Task;
import com.example.to_dolist.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "com.example.to_dolist.EXTRA_ID";
    public static final String EXTRA_TITLE = "com.example.to_dolist.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "com.example.to_dolist.EXTRA_DESCRIPTION";
    public static final String EXTRA_PRIORITY = "com.example.to_dolist.EXTRA_PRIORITY";
    public static final String EXTRA_DUE_DATE = "com.example.to_dolist.EXTRA_DUE_DATE";
    public static final String EXTRA_COMPLETED = "com.example.to_dolist.EXTRA_COMPLETED";

    private EditText editTextTitle;
    private EditText editTextDescription;
    private Spinner spinnerPriority;
    private TextView textDueDate;
    private TaskViewModel taskViewModel;
    private long selectedDueDate;
    private int taskId = -1;
    private boolean isCompleted;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        bindViews();
        setupPrioritySpinner();
        setupDatePicker();
        handleEditState();
        setupSaveButton();
    }

    private void bindViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        textDueDate = findViewById(R.id.textDueDateValue);
        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupPrioritySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.priority_options, android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
    }

    private void setupDatePicker() {
        selectedDueDate = System.currentTimeMillis();
        updateDueDateText();
    }

    private void handleEditState() {
        if (getIntent().hasExtra(EXTRA_ID)) {
            setTitle(R.string.edit_task);
            taskId = getIntent().getIntExtra(EXTRA_ID, -1);
            editTextTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
            editTextDescription.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
            String priority = getIntent().getStringExtra(EXTRA_PRIORITY);
            if (priority != null) {
                @SuppressWarnings("unchecked")
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerPriority.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(priority);
                    spinnerPriority.setSelection(position);
                }
            }
            selectedDueDate = getIntent().getLongExtra(EXTRA_DUE_DATE, System.currentTimeMillis());
            isCompleted = getIntent().getBooleanExtra(EXTRA_COMPLETED, false);
            updateDueDateText();
        } else {
            setTitle(R.string.add_task);
        }
    }

    private void setupSaveButton() {
        Button buttonSave = findViewById(R.id.buttonSaveTask);
        buttonSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDueDate);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = selectedDate.getTimeInMillis();
                    updateDueDateText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDueDateText() {
        textDueDate.setText(dateFormat.format(selectedDueDate));
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(title, description, priority, selectedDueDate, isCompleted);
        if (taskId != -1) {
            task.setId(taskId);
            taskViewModel.update(task);
        } else {
            taskViewModel.insert(task);
        }

        setResult(RESULT_OK);
        finish();
    }
}
