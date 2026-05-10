package com.example.to_dolist.data.local.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.to_dolist.data.local.dao.CategoryDao;
import com.example.to_dolist.data.local.dao.SubtaskDao;
import com.example.to_dolist.data.local.dao.TaskDao;
import com.example.to_dolist.data.local.entity.CategoryEntity;
import com.example.to_dolist.data.local.entity.SubtaskEntity;
import com.example.to_dolist.data.local.entity.TaskEntity;

@Database(
    entities = {TaskEntity.class, CategoryEntity.class, SubtaskEntity.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final RoomDatabase.Callback SEED_CATEGORIES = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("INSERT INTO categories (name, colorHex) VALUES ('General', '#5C6BC0')");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract CategoryDao categoryDao();
    public abstract SubtaskDao subtaskDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "task_database"  // keep original name so MIGRATION_1_2 runs on existing data
                    )
                    .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3)
                    .addCallback(SEED_CATEGORIES)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /** Closes and clears the singleton so the next {@link #getInstance} rebuilds (e.g. after restore). */
    public static synchronized void closeAndClearInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
