package com.example.to_dolist.presentation.addedit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.databinding.ItemSubtaskBinding;
import com.example.to_dolist.domain.model.Subtask;

public class SubtaskAdapter extends ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder> {

    public interface OnSubtaskToggle  { void onToggle(int index); }
    public interface OnSubtaskRemove  { void onRemove(int index); }

    private final OnSubtaskToggle onToggle;
    private final OnSubtaskRemove onRemove;

    public SubtaskAdapter(OnSubtaskToggle onToggle, OnSubtaskRemove onRemove) {
        super(DIFF_CALLBACK);
        this.onToggle = onToggle;
        this.onRemove = onRemove;
    }

    private static final DiffUtil.ItemCallback<Subtask> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Subtask>() {
                @Override
                public boolean areItemsTheSame(@NonNull Subtask o, @NonNull Subtask n) {
                    return o.getId() == n.getId() && o.getTitle().equals(n.getTitle());
                }
                @Override
                public boolean areContentsTheSame(@NonNull Subtask o, @NonNull Subtask n) {
                    return o.isCompleted() == n.isCompleted() && o.getTitle().equals(n.getTitle());
                }
            };

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubtaskBinding b = ItemSubtaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubtaskViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        holder.bind(getItem(position), position, onToggle, onRemove);
    }

    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubtaskBinding b;

        SubtaskViewHolder(ItemSubtaskBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Subtask subtask, int pos, OnSubtaskToggle toggle, OnSubtaskRemove remove) {
            b.checkboxSubtask.setOnCheckedChangeListener(null);
            b.checkboxSubtask.setText(subtask.getTitle());
            b.checkboxSubtask.setChecked(subtask.isCompleted());
            b.checkboxSubtask.setOnCheckedChangeListener((btn, checked) -> toggle.onToggle(pos));
            b.buttonRemoveSubtask.setOnClickListener(v -> remove.onRemove(pos));
        }
    }
}
