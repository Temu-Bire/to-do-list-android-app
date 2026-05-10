package com.example.to_dolist.presentation.home;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.databinding.ActivityHomeBinding;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.presentation.addedit.AddEditActivity;
import com.example.to_dolist.presentation.search.SearchActivity;
import com.example.to_dolist.presentation.settings.SettingsActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity implements TaskAdapter.TaskListener {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private TaskAdapter adapter;

    // Holds the last deleted task for Undo
    private Task lastDeletedTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        setupRecyclerView();
        setupSwipeToDelete();
        setupChips();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(this);
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTasks.setAdapter(adapter);
        binding.recyclerViewTasks.setHasFixedSize(true);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private final ColorDrawable deleteBackground =
                    new ColorDrawable(ContextCompat.getColor(HomeActivity.this, R.color.error_red));

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                lastDeletedTask = adapter.getTaskAt(position);
                viewModel.delete(lastDeletedTask);

                // Show Snackbar with UNDO
                Snackbar.make(binding.coordinatorLayout, R.string.task_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            if (lastDeletedTask != null) {
                                viewModel.insert(lastDeletedTask);
                                lastDeletedTask = null;
                            }
                        })
                        .setAnchorView(binding.fabAddTask)
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean active) {
                View itemView = vh.itemView;
                if (dX < 0) { // swiping left
                    deleteBackground.setBounds(
                            itemView.getRight() + (int) dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                } else {       // swiping right
                    deleteBackground.setBounds(
                            itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + (int) dX, itemView.getBottom());
                }
                deleteBackground.draw(c);
                super.onChildDraw(c, rv, vh, dX, dY, actionState, active);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerViewTasks);
    }

    private void setupChips() {
        // Filter chips
        binding.chipAll.setOnClickListener(v -> {
            viewModel.setFilter(HomeViewModel.FilterMode.ALL);
            updateChipSelection(binding.chipAll);
        });
        binding.chipCompleted.setOnClickListener(v -> {
            viewModel.setFilter(HomeViewModel.FilterMode.COMPLETED);
            updateChipSelection(binding.chipCompleted);
        });

        // Sort chips
        binding.chipSortDate.setOnClickListener(v -> viewModel.setSort(HomeViewModel.SortMode.BY_DATE));
        binding.chipSortPriority.setOnClickListener(v -> viewModel.setSort(HomeViewModel.SortMode.BY_PRIORITY));
    }

    private void updateChipSelection(Chip selected) {
        binding.chipAll.setChecked(selected == binding.chipAll);
        binding.chipCompleted.setChecked(selected == binding.chipCompleted);
    }

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditActivity.class)));
    }

    private void navigateToEditTask(Task task) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_TASK_ID,          task.getId());
        intent.putExtra(AddEditActivity.EXTRA_TASK_TITLE,       task.getTitle());
        intent.putExtra(AddEditActivity.EXTRA_TASK_DESC,        task.getDescription());
        intent.putExtra(AddEditActivity.EXTRA_TASK_PRIORITY,    task.getPriority().name());
        intent.putExtra(AddEditActivity.EXTRA_TASK_DUE_DATE,    task.getDueDate());
        intent.putExtra(AddEditActivity.EXTRA_TASK_COMPLETED,   task.isCompleted());
        intent.putExtra(AddEditActivity.EXTRA_TASK_REMINDER,    task.isReminderEnabled());
        intent.putExtra(AddEditActivity.EXTRA_TASK_RECURRING,   task.isRecurring());
        intent.putExtra(AddEditActivity.EXTRA_TASK_CATEGORY_ID, task.getCategoryId() != null ? task.getCategoryId() : -1);
        startActivity(intent);
    }

    private void observeData() {
        // Task list
        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            binding.textEmptyState.setVisibility(
                    tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Overdue badge
        viewModel.getOverdueTasks().observe(this, overdue -> {
            int count = overdue == null ? 0 : overdue.size();
            if (count > 0) {
                binding.textOverdueBadge.setVisibility(View.VISIBLE);
                binding.textOverdueBadge.setText(
                        getResources().getQuantityString(R.plurals.overdue_tasks, count, count));
            } else {
                binding.textOverdueBadge.setVisibility(View.GONE);
            }
        });

        // Error messages
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.coordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── TaskListener ─────────────────────────────────────────────────────────

    @Override
    public void onTaskEdit(Task task) {
        navigateToEditTask(task);
    }

    @Override
    public void onTaskDelete(Task task) {
        String title = task.getTitle() != null ? task.getTitle() : "";
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(getString(R.string.delete_task_message, title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, which) -> {
                    lastDeletedTask = task;
                    viewModel.delete(task);
                    Snackbar.make(binding.coordinatorLayout, R.string.task_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> {
                                if (lastDeletedTask != null) {
                                    viewModel.insert(lastDeletedTask);
                                    lastDeletedTask = null;
                                }
                            })
                            .setAnchorView(binding.fabAddTask)
                            .show();
                })
                .show();
    }

    @Override
    public void onCheckChanged(Task task, boolean checked) {
        task.setCompleted(checked);
        viewModel.update(task);
    }
}
