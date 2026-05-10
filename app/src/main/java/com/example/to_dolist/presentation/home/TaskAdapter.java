package com.example.to_dolist.presentation.home;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

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
import com.example.to_dolist.domain.model.TaskDisplayStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onTaskEdit(Task task);

        void onTaskDelete(Task task);

        void onCheckChanged(Task task, boolean checked);
    }

    private final TaskListener listener;
    private Map<Integer, String> categoryNames = new HashMap<>();

    public TaskAdapter(TaskListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Task t = getItem(position);
        return t != null ? t.getId() : RecyclerView.NO_ID;
    }

    public void setCategoryNames(Map<Integer, String> names) {
        categoryNames = names != null ? names : new HashMap<>();
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
                            && Objects.equals(o.getTitle(), n.getTitle())
                            && Objects.equals(o.getDescription(), n.getDescription())
                            && o.getPriority() == n.getPriority()
                            && o.getDueDate() == n.getDueDate()
                            && o.isOverdue() == n.isOverdue()
                            && o.getWorkflowStatus() == n.getWorkflowStatus()
                            && Objects.equals(o.getCategoryId(), n.getCategoryId())
                            && o.getSortOrder() == n.getSortOrder()
                            && o.getSubtaskCompletedCount() == n.getSubtaskCompletedCount()
                            && subtaskSize(o) == subtaskSize(n);
                }

                private int subtaskSize(Task t) {
                    return t.getSubtasks() == null ? 0 : t.getSubtasks().size();
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
        holder.bind(getItem(position), listener, categoryNames);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull TaskViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(), android.R.anim.fade_in));
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    public void moveItem(int from, int to, Runnable commitCallback) {
        List<Task> cur = getCurrentList();
        if (from < 0 || to < 0 || from >= cur.size() || to >= cur.size()) return;
        java.util.List<Task> next = new java.util.ArrayList<>(cur);
        java.util.Collections.swap(next, from, to);
        submitList(next, commitCallback);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskBinding b;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Task task, TaskListener listener, Map<Integer, String> categoryNames) {
            Context ctx = itemView.getContext();

            b.textTitle.setText(task.getTitle() != null ? task.getTitle() : "");
            if (task.isCompleted()) {
                b.textTitle.setPaintFlags(b.textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                b.textTitle.setAlpha(0.55f);
            } else {
                b.textTitle.setPaintFlags(b.textTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                b.textTitle.setAlpha(1.0f);
            }

            TaskDisplayStatus dStatus = task.getDisplayStatus();
            b.chipStatus.setText(UiResources.displayStatusLabel(ctx, dStatus));
            int statusBg;
            int statusFg = ContextCompat.getColor(ctx, android.R.color.white);
            switch (dStatus) {
                case COMPLETED:
                    statusBg = ContextCompat.getColor(ctx, R.color.status_completed);
                    break;
                case OVERDUE:
                    statusBg = ContextCompat.getColor(ctx, R.color.error_red);
                    break;
                case IN_PROGRESS:
                    statusBg = ContextCompat.getColor(ctx, R.color.status_in_progress);
                    break;
                case PENDING:
                default:
                    statusBg = ContextCompat.getColor(ctx, R.color.status_pending);
                    break;
            }
            b.chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(statusBg));
            b.chipStatus.setTextColor(statusFg);

            Integer cid = task.getCategoryId();
            if (cid != null && categoryNames != null && categoryNames.containsKey(cid)) {
                b.textCategory.setVisibility(View.VISIBLE);
                b.textCategory.setText(categoryNames.get(cid));
            } else {
                b.textCategory.setVisibility(View.GONE);
            }

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                b.textDescription.setVisibility(View.VISIBLE);
                b.textDescription.setText(task.getDescription());
            } else {
                b.textDescription.setVisibility(View.GONE);
            }

            b.textDueDate.setText(DateUtils.toFriendlyLabel(ctx, task.getDueDate()));
            if (task.isOverdue()) {
                b.textDueDate.setTextColor(ContextCompat.getColor(ctx, R.color.error_red));
                b.iconOverdue.setVisibility(View.VISIBLE);
            } else {
                b.textDueDate.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary));
                b.iconOverdue.setVisibility(View.GONE);
            }

            b.chipPriority.setText(UiResources.priorityLabel(ctx, task.getPriority()));
            int colorRes;
            switch (task.getPriority()) {
                case HIGH:
                    colorRes = R.color.priority_high;
                    break;
                case MEDIUM:
                    colorRes = R.color.priority_medium;
                    break;
                default:
                    colorRes = R.color.priority_low;
                    break;
            }
            b.chipPriority.setChipBackgroundColorResource(colorRes);

            int total = task.getSubtasks() != null ? task.getSubtasks().size() : 0;
            int completed = task.getSubtaskCompletedCount();
            if (total > 0) {
                b.layoutSubtaskProgress.setVisibility(View.VISIBLE);
                b.textSubtaskCount.setText(completed + "/" + total);
                b.progressSubtasks.setMax(total);
                b.progressSubtasks.setProgress(completed);
            } else {
                b.layoutSubtaskProgress.setVisibility(View.GONE);
            }

            b.iconReminder.setVisibility(task.isReminderEnabled() ? View.VISIBLE : View.GONE);

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
