package com.example.to_dolist.core.util;

import android.content.Context;

import com.example.to_dolist.R;
import com.example.to_dolist.domain.model.Priority;
import com.example.to_dolist.domain.model.TaskDisplayStatus;

/** UI-facing labels backed by strings (localized). */
public final class UiResources {

    private UiResources() {}

    public static String priorityLabel(Context context, Priority priority) {
        if (priority == null) priority = Priority.LOW;
        switch (priority) {
            case HIGH:   return context.getString(R.string.priority_high);
            case MEDIUM: return context.getString(R.string.priority_medium);
            default:     return context.getString(R.string.priority_low);
        }
    }

    public static String displayStatusLabel(Context context, TaskDisplayStatus status) {
        if (status == null) status = TaskDisplayStatus.PENDING;
        switch (status) {
            case IN_PROGRESS:
                return context.getString(R.string.status_chip_progress);
            case COMPLETED:
                return context.getString(R.string.status_chip_completed);
            case OVERDUE:
                return context.getString(R.string.status_chip_overdue);
            case PENDING:
            default:
                return context.getString(R.string.status_chip_pending);
        }
    }
}
