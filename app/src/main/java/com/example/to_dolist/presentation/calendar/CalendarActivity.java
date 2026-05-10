package com.example.to_dolist.presentation.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.DateUtils;
import com.example.to_dolist.databinding.ItemCalendarTaskBinding;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.databinding.ActivityCalendarBinding;
import com.example.to_dolist.presentation.BaseActivity;
import com.example.to_dolist.presentation.addedit.AddEditActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarActivity extends BaseActivity {

    private ActivityCalendarBinding binding;
    private CalendarTaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Calendar cal = Calendar.getInstance();
        binding.textMonthTitle.setText(new SimpleDateFormat("LLLL yyyy", Locale.getDefault())
                .format(cal.getTime()));

        adapter = new CalendarTaskAdapter(task -> {
            Intent i = new Intent(this, AddEditActivity.class);
            i.putExtra(AddEditActivity.EXTRA_TASK_ID, task.getId());
            i.putExtra(AddEditActivity.EXTRA_TASK_TITLE, task.getTitle());
            i.putExtra(AddEditActivity.EXTRA_TASK_DESC, task.getDescription());
            i.putExtra(AddEditActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
            i.putExtra(AddEditActivity.EXTRA_TASK_DUE_DATE, task.getDueDate());
            i.putExtra(AddEditActivity.EXTRA_TASK_COMPLETED, task.isCompleted());
            i.putExtra(AddEditActivity.EXTRA_TASK_REMINDER, task.isReminderEnabled());
            i.putExtra(AddEditActivity.EXTRA_TASK_RECURRING, task.isRecurring());
            i.putExtra(AddEditActivity.EXTRA_TASK_CATEGORY_ID,
                    task.getCategoryId() != null ? task.getCategoryId() : -1);
            i.putExtra(AddEditActivity.EXTRA_TASK_WORKFLOW, task.getWorkflowStatus().name());
            i.putExtra(AddEditActivity.EXTRA_TASK_SORT_ORDER, task.getSortOrder());
            startActivity(i);
        });
        binding.recyclerCalendarTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCalendarTasks.setAdapter(adapter);

        CalendarViewModel vm = new ViewModelProvider(this).get(CalendarViewModel.class);
        vm.getTasksThisMonth().observe(this, adapter::submitList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    static class CalendarTaskAdapter extends ListAdapter<Task, CalendarTaskAdapter.Holder> {

        interface Listener {
            void onOpen(Task task);
        }

        private final Listener listener;

        CalendarTaskAdapter(Listener listener) {
            super(DIFF);
            this.listener = listener;
        }

        private static final DiffUtil.ItemCallback<Task> DIFF = new DiffUtil.ItemCallback<Task>() {
            @Override
            public boolean areItemsTheSame(@NonNull Task o, @NonNull Task n) {
                return o.getId() == n.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Task o, @NonNull Task n) {
                return o.isCompleted() == n.isCompleted()
                        && o.getDueDate() == n.getDueDate()
                        && Objects.equals(o.getTitle(), n.getTitle());
            }
        };

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCalendarTaskBinding b = ItemCalendarTaskBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new Holder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bind(getItem(position), listener);
        }

        static class Holder extends RecyclerView.ViewHolder {
            private final ItemCalendarTaskBinding b;

            Holder(ItemCalendarTaskBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            void bind(Task task, Listener listener) {
                b.textTitle.setText(task.getTitle());
                b.textDay.setText(DateUtils.toFriendlyLabel(b.getRoot().getContext(), task.getDueDate()));
                b.textStatus.setText(task.isCompleted()
                        ? b.getRoot().getContext().getString(R.string.status_chip_completed)
                        : b.getRoot().getContext().getString(R.string.status_chip_pending));
                b.getRoot().setOnClickListener(v -> listener.onOpen(task));
            }
        }
    }
}
