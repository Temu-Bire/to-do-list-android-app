package com.example.to_dolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.data.Task;
import com.example.to_dolist.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskActionListener {

    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private TextView textEmptyState;
    private Spinner spinnerFilter;
    private Spinner spinnerSort;
    private LiveData<List<Task>> currentSource;

    private final ActivityResultLauncher<Intent> taskEditorLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        setupRecyclerView();
        setupSpinners();
        observeTasks();
    }

    private void setupViews() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        textEmptyState = findViewById(R.id.textEmptyState);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        spinnerSort = findViewById(R.id.spinnerSort);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            taskEditorLauncher.launch(intent);
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setHasFixedSize(true);
        taskAdapter = new TaskAdapter(this);
        recyclerViewTasks.setAdapter(taskAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Task task = taskAdapter.getTaskAt(viewHolder.getAdapterPosition());
                taskViewModel.delete(task);
                Toast.makeText(MainActivity.this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewTasks);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.filter_options, android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(this::observeTasks));
        spinnerSort.setOnItemSelectedListener(new SimpleItemSelectedListener(this::observeTasks));
    }

    private void observeTasks() {
        if (currentSource != null) {
            currentSource.removeObservers(this);
        }

        String filter = spinnerFilter.getSelectedItem().toString();
        String sort = spinnerSort.getSelectedItem().toString();

        if (getString(R.string.filter_completed).equals(filter)) {
            currentSource = taskViewModel.getCompletedTasks();
        } else if (getString(R.string.sort_priority).equals(sort)) {
            currentSource = taskViewModel.getSortedByPriority();
        } else {
            currentSource = taskViewModel.getSortedByDate();
        }

        currentSource.observe(this, tasks -> {
            taskAdapter.submitList(tasks);
            textEmptyState.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_TITLE, task.getTitle());
        intent.putExtra(AddEditTaskActivity.EXTRA_DESCRIPTION, task.getDescription());
        intent.putExtra(AddEditTaskActivity.EXTRA_PRIORITY, task.getPriority());
        intent.putExtra(AddEditTaskActivity.EXTRA_DUE_DATE, task.getDueDate());
        intent.putExtra(AddEditTaskActivity.EXTRA_COMPLETED, task.isCompleted());
        taskEditorLauncher.launch(intent);
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        taskViewModel.update(task);
    }
}
