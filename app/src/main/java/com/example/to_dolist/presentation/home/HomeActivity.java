package com.example.to_dolist.presentation.home;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.databinding.ActivityHomeBinding;
import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.presentation.addedit.AddEditActivity;
import com.example.to_dolist.presentation.calendar.CalendarActivity;
import com.example.to_dolist.presentation.dashboard.DashboardActivity;
import com.example.to_dolist.presentation.search.SearchActivity;
import com.example.to_dolist.presentation.BaseActivity;
import com.example.to_dolist.presentation.settings.SettingsActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends BaseActivity implements TaskAdapter.TaskListener {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private TaskAdapter adapter;
    private Task lastDeletedTask;

    private final List<Category> categoryListForSpinner = new ArrayList<>();
    private Integer selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupSwipeAndDrag();
        setupFilterChips();
        setupSortChips();
        setupCategorySpinner();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(this);
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTasks.setAdapter(adapter);
        binding.recyclerViewTasks.setHasFixedSize(true);
        binding.recyclerViewTasks.setItemViewCacheSize(20);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(true);
        animator.setMoveDuration(180);
        binding.recyclerViewTasks.setItemAnimator(animator);
    }

    private void setupSwipeAndDrag() {
        ColorDrawable deleteBackground =
                new ColorDrawable(ContextCompat.getColor(this, R.color.error_red));

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean isLongPressDragEnabled() {
                return viewModel.getCurrentSort() == HomeViewModel.SortMode.BY_DATE;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                if (viewModel.getCurrentSort() != HomeViewModel.SortMode.BY_DATE) return false;
                int from = vh.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;
                adapter.moveItem(from, to, () ->
                        viewModel.persistTaskOrder(new ArrayList<>(adapter.getCurrentList())));
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                lastDeletedTask = adapter.getTaskAt(position);
                viewModel.delete(lastDeletedTask);

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
                if (dX != 0) {
                    if (dX < 0) {
                        deleteBackground.setBounds(
                                itemView.getRight() + (int) dX, itemView.getTop(),
                                itemView.getRight(), itemView.getBottom());
                    } else {
                        deleteBackground.setBounds(
                                itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + (int) dX, itemView.getBottom());
                    }
                    deleteBackground.draw(c);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, active);
            }
        });
        helper.attachToRecyclerView(binding.recyclerViewTasks);
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> selectFilterChip(binding.chipAll, HomeViewModel.FilterMode.ALL));
        binding.chipActive.setOnClickListener(v -> selectFilterChip(binding.chipActive, HomeViewModel.FilterMode.ACTIVE));
        binding.chipInProgress.setOnClickListener(v -> selectFilterChip(binding.chipInProgress, HomeViewModel.FilterMode.IN_PROGRESS));
        binding.chipOverdue.setOnClickListener(v -> selectFilterChip(binding.chipOverdue, HomeViewModel.FilterMode.OVERDUE));
        binding.chipCompleted.setOnClickListener(v -> selectFilterChip(binding.chipCompleted, HomeViewModel.FilterMode.COMPLETED));
    }

    private void selectFilterChip(Chip selected, HomeViewModel.FilterMode mode) {
        binding.chipGroupFilter.check(selected.getId());
        viewModel.setFilter(mode);
    }

    private void setupSortChips() {
        binding.chipSortDate.setOnClickListener(v -> {
            binding.chipGroupSort.check(binding.chipSortDate.getId());
            viewModel.setSort(HomeViewModel.SortMode.BY_DATE);
        });
        binding.chipSortPriority.setOnClickListener(v -> {
            binding.chipGroupSort.check(binding.chipSortPriority.getId());
            viewModel.setSort(HomeViewModel.SortMode.BY_PRIORITY);
        });
    }

    private void setupCategorySpinner() {
        Spinner spinner = binding.spinnerCategory;
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > categoryListForSpinner.size()) {
                    selectedCategoryId = null;
                    viewModel.setCategoryFilter(null);
                    return;
                }
                Category cat = categoryListForSpinner.get(position - 1);
                selectedCategoryId = cat.getId();
                viewModel.setCategoryFilter(selectedCategoryId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCategoryFilter(null);
            }
        });
    }

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditActivity.class)));
    }

    private void navigateToEditTask(Task task) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(AddEditActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(AddEditActivity.EXTRA_TASK_DESC, task.getDescription());
        intent.putExtra(AddEditActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
        intent.putExtra(AddEditActivity.EXTRA_TASK_DUE_DATE, task.getDueDate());
        intent.putExtra(AddEditActivity.EXTRA_TASK_COMPLETED, task.isCompleted());
        intent.putExtra(AddEditActivity.EXTRA_TASK_REMINDER, task.isReminderEnabled());
        intent.putExtra(AddEditActivity.EXTRA_TASK_RECURRING, task.isRecurring());
        intent.putExtra(AddEditActivity.EXTRA_TASK_CATEGORY_ID,
                task.getCategoryId() != null ? task.getCategoryId() : -1);
        intent.putExtra(AddEditActivity.EXTRA_TASK_WORKFLOW, task.getWorkflowStatus().name());
        intent.putExtra(AddEditActivity.EXTRA_TASK_SORT_ORDER, task.getSortOrder());
        startActivity(intent);
    }

    private void observeData() {
        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks != null ? new ArrayList<>(tasks) : new ArrayList<>());
            boolean empty = tasks == null || tasks.isEmpty();
            binding.layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerViewTasks.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getCategories().observe(this, categories -> {
            Map<Integer, String> map = new HashMap<>();
            categoryListForSpinner.clear();
            if (categories != null) {
                categoryListForSpinner.addAll(categories);
                for (Category c : categories) {
                    map.put(c.getId(), c.getName());
                }
            }
            adapter.setCategoryNames(map);
            adapter.submitList(new ArrayList<>(adapter.getCurrentList()));

            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.category_all));
            for (Category c : categoryListForSpinner) {
                labels.add(c.getName() != null ? c.getName() : "");
            }
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> spAdapter = (ArrayAdapter<String>) binding.spinnerCategory.getAdapter();
            spAdapter.clear();
            spAdapter.addAll(labels);
            spAdapter.notifyDataSetChanged();

            if (selectedCategoryId != null) {
                for (int i = 0; i < categoryListForSpinner.size(); i++) {
                    if (categoryListForSpinner.get(i).getId() == selectedCategoryId) {
                        binding.spinnerCategory.setSelection(i + 1, false);
                        break;
                    }
                }
            }
        });

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
        } else if (itemId == R.id.action_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            return true;
        } else if (itemId == R.id.action_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (checked) {
            task.setWorkflowStatus(com.example.to_dolist.domain.model.TaskWorkflowStatus.PENDING);
        }
        viewModel.update(task);
    }
}
