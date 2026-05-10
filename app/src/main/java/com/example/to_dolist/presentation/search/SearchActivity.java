package com.example.to_dolist.presentation.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.to_dolist.R;
import com.example.to_dolist.databinding.ActivitySearchBinding;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.presentation.addedit.AddEditActivity;
import com.example.to_dolist.presentation.home.TaskAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchActivity extends AppCompatActivity implements TaskAdapter.TaskListener {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private TaskAdapter adapter;
    private Task lastDeletedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        adapter = new TaskAdapter(this);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setQuery(newText);
                return true;
            }
        });

        viewModel.getSearchResults().observe(this, tasks -> adapter.submitList(tasks));
    }

    @Override
    public void onTaskEdit(Task task) {
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
                    Snackbar.make(binding.getRoot(), R.string.task_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> {
                                if (lastDeletedTask != null) {
                                    viewModel.restore(lastDeletedTask);
                                    lastDeletedTask = null;
                                }
                            })
                            .show();
                })
                .show();
    }

    @Override
    public void onCheckChanged(Task task, boolean checked) {
        task.setCompleted(checked);
        viewModel.update(task);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
