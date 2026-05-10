package com.example.to_dolist.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.example.to_dolist.core.util.NotificationHelper;
import com.example.to_dolist.data.local.db.AppDatabase;
import com.example.to_dolist.data.local.entity.TaskEntity;
import com.example.to_dolist.worker.ReminderWorker;

import java.util.concurrent.Executors;

/**
 * Handles notification quick actions: mark complete and snooze.
 */
public class TaskActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        int taskId = intent.getIntExtra(NotificationHelper.EXTRA_TASK_ID, -1);
        if (taskId < 0) return;

        if (NotificationHelper.ACTION_COMPLETE.equals(intent.getAction())) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                TaskEntity e = db.taskDao().findByIdSync(taskId);
                if (e != null && !e.isCompleted()) {
                    e.setCompleted(true);
                    e.setWorkflowStatus("PENDING");
                    db.taskDao().update(e);
                }
            });
            ReminderWorker.cancel(context, taskId);
            NotificationManagerCompat.from(context).cancel(taskId);
        } else if (NotificationHelper.ACTION_SNOOZE.equals(intent.getAction())) {
            String title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE);
            if (title == null) title = "";
            ReminderWorker.scheduleSnooze(context, taskId, title);
            NotificationManagerCompat.from(context).cancel(taskId);
        }
    }
}
