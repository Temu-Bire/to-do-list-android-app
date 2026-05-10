package com.example.to_dolist.data.local.db;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * All Room database migrations live here.
 *
 * Senior engineer note: Never use fallbackToDestructiveMigration() in production.
 * Always write explicit migrations so users' data is preserved on updates.
 */
public class Migrations {

    /**
     * v1 → v2: Restructured tasks table + added categories and subtasks tables.
     * The old "tasks" table stored priority/isCompleted.
     * New schema renames isCompleted → completed and adds new columns.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Rename old table
            database.execSQL("ALTER TABLE tasks RENAME TO tasks_old");

            // 2. Create new tasks table
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

            // 3. Copy data from old table (isCompleted → completed)
            database.execSQL("INSERT INTO tasks " +
                    "(id, title, description, priority, dueDate, completed, " +
                    " categoryId, reminderEnabled, recurring, recurrenceRule) " +
                    "SELECT id, title, description, priority, dueDate, isCompleted, " +
                    "NULL, 0, 0, NULL FROM tasks_old");

            // 4. Drop old table
            database.execSQL("DROP TABLE tasks_old");

            // 5. Create categories table
            database.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "colorHex TEXT)");

            // 6. Create subtasks table
            database.execSQL("CREATE TABLE IF NOT EXISTS subtasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "taskId INTEGER NOT NULL, " +
                    "title TEXT, " +
                    "completed INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)");
        }
    };
}
