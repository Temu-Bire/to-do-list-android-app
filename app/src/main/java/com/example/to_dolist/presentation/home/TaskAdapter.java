package com.example.to_dolist.presentation.home;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.DateUtils;
import com.example.to_dolist.core.util.UiResources;
import com.example.to_dolist.databinding.ItemTaskBinding;
import com.example.to_dolist.domain.model.Task;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onTaskEdit(Task task);

        void onTaskDelete(Task task);

        void onCheckChanged(Task task, boolean checked);
    }

    private final TaskListener listener;

    public TaskAdapter(TaskListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Task>() {
                @Override
                public boolean areItemsTheSame(@NonNull Task o, @NonNull Task n) {
                    return o.getId() == n.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Task o, @NonNull Task n) {
                    return o.isCompleted() == n.isCompleted()
                            && o.getTitle().equals(n.getTitle())
                            && o.getPriority() == n.getPriority()
                            && o.getDueDate() == n.getDueDate()
                            && o.isOverdue() == n.isOverdue();
                }
            };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskBinding b;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Task task, TaskListener listener) {
            Context ctx = itemView.getContext();

            // Title — strikethrough if completed
            b.textTitle.setText(task.getTitle());
            if (task.isCompleted()) {
                b.textTitle.setPaintFlags(b.textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                b.textTitle.setAlpha(0.5f);
            } else {
                b.textTitle.setPaintFlags(b.textTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                b.textTitle.setAlpha(1.0f);
            }

            // Description
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                b.textDescription.setVisibility(View.VISIBLE);
                b.textDescription.setText(task.getDescription());
            } else {
                b.textDescription.setVisibility(View.GONE);
            }

            // Due date — red if overdue
            b.textDueDate.setText(DateUtils.toFriendlyLabel(ctx, task.getDueDate()));
            if (task.isOverdue()) {
                b.textDueDate.setTextColor(ContextCompat.getColor(ctx, R.color.error_red));
                b.iconOverdue.setVisibility(View.VISIBLE);
            } else {
                b.textDueDate.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary));
                b.iconOverdue.setVisibility(View.GONE);
            }

            // Priority chip color
            b.chipPriority.setText(UiResources.priorityLabel(ctx, task.getPriority()));
            int colorRes;
            switch (task.getPriority()) {
                case HIGH:   colorRes = R.color.priority_high;   break;
                case MEDIUM: colorRes = R.color.priority_medium; break;
                default:     colorRes = R.color.priority_low;    break;
            }
            b.chipPriority.setChipBackgroundColorResource(colorRes);

            // Subtask progress
            int total     = task.getSubtasks() != null ? task.getSubtasks().size() : 0;
            int completed = task.getSubtaskCompletedCount();
            if (total > 0) {
                b.layoutSubtaskProgress.setVisibility(View.VISIBLE);
                b.textSubtaskCount.setText(completed + "/" + total);
                b.progressSubtasks.setMax(total);
                b.progressSubtasks.setProgress(completed);
            } else {
                b.layoutSubtaskProgress.setVisibility(View.GONE);
            }

            // Reminder indicator
            b.iconReminder.setVisibility(task.isReminderEnabled() ? View.VISIBLE : View.GONE);

            // Checkbox
            b.checkboxComplete.setOnCheckedChangeListener(null);
            b.checkboxComplete.setChecked(task.isCompleted());
            b.checkboxComplete.setOnCheckedChangeListener(
                    (btn, checked) -> listener.onCheckChanged(task, checked));

            b.buttonEdit.setOnClickListener(v -> listener.onTaskEdit(task));

            b.buttonDelete.setOnClickListener(v -> listener.onTaskDelete(task));

            itemView.setOnClickListener(v -> listener.onTaskEdit(task));
        }
    }
}
