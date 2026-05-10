package com.example.to_dolist.core.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.to_dolist.R;
import com.example.to_dolist.notification.TaskActionReceiver;
import com.example.to_dolist.presentation.home.HomeActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID = "todo_reminders";
    public static final String CHANNEL_NAME = "Task Reminders";

    public static final String ACTION_COMPLETE = "com.example.to_dolist.ACTION_TASK_COMPLETE";
    public static final String ACTION_SNOOZE = "com.example.to_dolist.ACTION_TASK_SNOOZE";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for upcoming tasks");
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void showReminder(Context context, int taskId, String taskTitle) {
        Intent openIntent = new Intent(context, HomeActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent contentPi = PendingIntent.getActivity(context, taskId, openIntent, flags);

        Intent completeIntent = new Intent(context, TaskActionReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE);
        completeIntent.putExtra(EXTRA_TASK_ID, taskId);
        completeIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        PendingIntent completePi = PendingIntent.getBroadcast(
                context, taskId * 10 + 1, completeIntent, flags);

        Intent snoozeIntent = new Intent(context, TaskActionReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_TASK_ID, taskId);
        snoozeIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        PendingIntent snoozePi = PendingIntent.getBroadcast(
                context, taskId * 10 + 2, snoozeIntent, flags);

        String titleLabel = taskTitle != null ? taskTitle : "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_task_due_soon))
                .setContentText(titleLabel)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentPi)
                .addAction(0, context.getString(R.string.notif_action_complete), completePi)
                .addAction(0, context.getString(R.string.notif_action_snooze), snoozePi);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(taskId, builder.build());
    }
}
