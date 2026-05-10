package com.example.to_dolist.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.to_dolist.core.util.DateUtils;
import com.example.to_dolist.core.util.NotificationHelper;

import java.util.concurrent.TimeUnit;

/**
 * WorkManager worker that fires a notification 1 hour before a task's due date.
 *
 * Senior engineer note: WorkManager survives app kills and device restarts.
 * It is the correct solution for guaranteed deferred work — AlarmManager is needed
 * only for exact-time alarms on Android 12+ with SCHEDULE_EXACT_ALARM permission.
 */
public class ReminderWorker extends Worker {

    public static final String KEY_TASK_ID    = "task_id";
    public static final String KEY_TASK_TITLE = "task_title";

    private static final long LEAD_TIME_MS = 60 * 60 * 1000L; // 1 hour

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        int    taskId    = getInputData().getInt(KEY_TASK_ID, -1);
        String taskTitle = getInputData().getString(KEY_TASK_TITLE);

        if (taskId == -1 || taskTitle == null) return Result.failure();

        NotificationHelper.showReminder(getApplicationContext(), taskId, taskTitle);
        return Result.success();
    }

    // ─── Static factory helpers ───────────────────────────────────────────────

    /** Schedule a reminder 1 hour before the given dueDate. */
    public static void schedule(Context context, int taskId, String taskTitle, long dueDate) {
        long delay = DateUtils.millisUntilReminderBefore(dueDate, LEAD_TIME_MS);

        Data inputData = new Data.Builder()
                .putInt(KEY_TASK_ID, taskId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_" + taskId)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "reminder_" + taskId,
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        request
                );
    }

    /** Cancel any pending reminder for a specific task. */
    public static void cancel(Context context, int taskId) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_" + taskId);
    }
}
