package com.example.to_dolist.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.R;
import com.example.to_dolist.data.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    public interface TaskActionListener {
        void onTaskClicked(Task task);
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }

    private final TaskActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public TaskAdapter(TaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getPriority().equals(newItem.getPriority())
                    && oldItem.getDueDate() == newItem.getDueDate()
                    && oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.textTitle.setText(task.getTitle());
        holder.textDescription.setText(task.getDescription());
        holder.textDueDate.setText(holder.itemView.getContext().getString(
                R.string.due_date_label, dateFormat.format(new Date(task.getDueDate()))
        ));
        holder.textPriority.setText(task.getPriority());
        holder.checkboxComplete.setOnCheckedChangeListener(null);
        holder.checkboxComplete.setChecked(task.isCompleted());
        holder.checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onTaskCheckedChanged(task, isChecked));

        int colorRes = R.color.priority_low;
        if ("High".equals(task.getPriority())) {
            colorRes = R.color.priority_high;
        } else if ("Medium".equals(task.getPriority())) {
            colorRes = R.color.priority_medium;
        }
        holder.textPriority.setBackgroundResource(R.drawable.bg_priority_chip);
        holder.textPriority.getBackground().setTint(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );

        holder.itemView.setOnClickListener(v -> listener.onTaskClicked(task));
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textDueDate;
        private final TextView textPriority;
        private final CheckBox checkboxComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDueDate = itemView.findViewById(R.id.textDueDate);
            textPriority = itemView.findViewById(R.id.textPriority);
            checkboxComplete = itemView.findViewById(R.id.checkboxComplete);
        }
    }
}
