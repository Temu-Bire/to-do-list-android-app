package com.example.to_dolist.core.util;

import android.content.Context;
import android.net.Uri;

import com.example.to_dolist.data.local.db.AppDatabase;
import com.example.to_dolist.data.local.entity.CategoryEntity;
import com.example.to_dolist.data.local.entity.TaskEntity;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Exports tasks and categories to CSV (UTF-8).
 */
public final class DataExportHelper {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private DataExportHelper() {}

    public interface Callback {
        void onDone(boolean success);
    }

    public static void exportTasksCsv(Context context, Uri destination, Callback callback) {
        Context app = context.getApplicationContext();
        IO.execute(() -> {
            boolean ok = false;
            try (OutputStream out = app.getContentResolver().openOutputStream(destination);
                 OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                if (out == null) {
                    if (callback != null) callback.onDone(false);
                    return;
                }
                w.write("id,title,description,priority,dueDate,completed,categoryId,workflowStatus,sortOrder,reminderEnabled,recurring,recurrenceRule\n");
                List<TaskEntity> tasks = AppDatabase.getInstance(app).taskDao().getAllTasksSnapshot();
                for (TaskEntity t : tasks) {
                    w.write(String.valueOf(t.getId()));
                    w.write(',');
                    w.write(csv(t.getTitle()));
                    w.write(',');
                    w.write(csv(t.getDescription()));
                    w.write(',');
                    w.write(csv(t.getPriority()));
                    w.write(',');
                    w.write(String.valueOf(t.getDueDate()));
                    w.write(',');
                    w.write(t.isCompleted() ? "1" : "0");
                    w.write(',');
                    w.write(t.getCategoryId() == null ? "" : String.valueOf(t.getCategoryId()));
                    w.write(',');
                    w.write(csv(t.getWorkflowStatus()));
                    w.write(',');
                    w.write(String.valueOf(t.getSortOrder()));
                    w.write(',');
                    w.write(t.isReminderEnabled() ? "1" : "0");
                    w.write(',');
                    w.write(t.isRecurring() ? "1" : "0");
                    w.write(',');
                    w.write(csv(t.getRecurrenceRule()));
                    w.write('\n');
                }
                w.write("\n# categories\n");
                w.write("id,name,colorHex\n");
                List<CategoryEntity> cats = AppDatabase.getInstance(app).categoryDao().getAllCategoriesSnapshot();
                for (CategoryEntity c : cats) {
                    w.write(String.valueOf(c.getId()));
                    w.write(',');
                    w.write(csv(c.getName()));
                    w.write(',');
                    w.write(csv(c.getColorHex()));
                    w.write('\n');
                }
                ok = true;
            } catch (Exception ignored) {
                ok = false;
            }
            if (callback != null) callback.onDone(ok);
        });
    }

    private static String csv(String s) {
        if (s == null) return "";
        String x = s.replace("\"", "\"\"");
        if (x.contains(",") || x.contains("\"") || x.contains("\n")) {
            return "\"" + x + "\"";
        }
        return x;
    }
}
