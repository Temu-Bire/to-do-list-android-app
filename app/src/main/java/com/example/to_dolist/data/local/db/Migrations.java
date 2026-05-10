package com.example.to_dolist.data.local.db;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * All Room database migrations live here.
 */
public class Migrations {

    /**
     * v1 → v2: Restructured tasks table + added categories and subtasks tables.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks RENAME TO tasks_old");

            database.execSQL("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "priority TEXT, " +
                    "dueDate INTEGER NOT NULL, " +
                    "completed INTEGER NOT NULL DEFAULT 0, " +
                    "categoryId INTEGER, " +
                    "reminderEnabled INTEGER NOT NULL DEFAULT 0, " +
                    "recurring INTEGER NOT NULL DEFAULT 0, " +
                    "recurrenceRule TEXT)");

            database.execSQL("INSERT INTO tasks " +
                    "(id, title, description, priority, dueDate, completed, " +
                    " categoryId, reminderEnabled, recurring, recurrenceRule) " +
                    "SELECT id, title, description, priority, dueDate, isCompleted, " +
                    "NULL, 0, 0, NULL FROM tasks_old");

            database.execSQL("DROP TABLE tasks_old");

            database.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "colorHex TEXT)");

            database.execSQL("CREATE TABLE IF NOT EXISTS subtasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "taskId INTEGER NOT NULL, " +
                    "title TEXT, " +
                    "completed INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)");
        }
    };

    /**
     * v2 → v3: Added workflowStatus and sortOrder.
     * workflowStatus is NOT NULL to match @NonNull in TaskEntity.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN workflowStatus TEXT NOT NULL DEFAULT 'PENDING'");
            database.execSQL("ALTER TABLE tasks ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0");
            database.execSQL("UPDATE tasks SET sortOrder = id * 1000 WHERE id > 0");

            database.execSQL(
                    "INSERT INTO categories (name, colorHex) " +
                    "SELECT 'General', '#5C6BC0' WHERE NOT EXISTS (SELECT 1 FROM categories LIMIT 1)");
        }
    };
}
