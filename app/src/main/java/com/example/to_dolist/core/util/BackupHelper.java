package com.example.to_dolist.core.util;

import android.content.Context;
import android.net.Uri;

import com.example.to_dolist.data.local.db.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copies the Room SQLite file to/from a user-chosen URI (SAF).
 * Restore requires an app restart to reopen the database safely.
 */
public final class BackupHelper {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private BackupHelper() {}

    public interface Callback {
        void onDone(boolean success);
    }

    public static void exportDatabase(Context context, Uri destination, Callback callback) {
        Context app = context.getApplicationContext();
        IO.execute(() -> {
            boolean ok = false;
            try {
                AppDatabase.closeAndClearInstance();
                File dbFile = app.getDatabasePath("task_database");
                try (InputStream in = new FileInputStream(dbFile);
                     OutputStream out = app.getContentResolver().openOutputStream(destination)) {
                    if (out == null) {
                        AppDatabase.getInstance(app);
                        if (callback != null) callback.onDone(false);
                        return;
                    }
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        out.write(buf, 0, n);
                    }
                    ok = true;
                }
            } catch (Exception ignored) {
                ok = false;
            } finally {
                AppDatabase.getInstance(app);
            }
            if (callback != null) callback.onDone(ok);
        });
    }

    public static void importDatabase(Context context, Uri source, Callback callback) {
        Context app = context.getApplicationContext();
        IO.execute(() -> {
            boolean ok = false;
            try {
                AppDatabase.closeAndClearInstance();
                File dbFile = app.getDatabasePath("task_database");
                try (InputStream in = app.getContentResolver().openInputStream(source);
                     OutputStream out = new java.io.FileOutputStream(dbFile)) {
                    if (in == null) {
                        AppDatabase.getInstance(app);
                        if (callback != null) callback.onDone(false);
                        return;
                    }
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        out.write(buf, 0, n);
                    }
                    ok = true;
                }
            } catch (Exception ignored) {
                ok = false;
            } finally {
                AppDatabase.getInstance(app);
            }
            if (callback != null) callback.onDone(ok);
        });
    }
}
