package com.example.to_dolist.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.to_dolist.data.local.dao.CategoryDao;
import com.example.to_dolist.data.local.dao.SubtaskDao;
import com.example.to_dolist.data.local.dao.TaskDao;
import com.example.to_dolist.data.local.entity.CategoryEntity;
import com.example.to_dolist.data.local.entity.SubtaskEntity;
import com.example.to_dolist.data.local.entity.TaskEntity;

@Database(
    entities = {TaskEntity.class, CategoryEntity.class, SubtaskEntity.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

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
                    .addMigrations(Migrations.MIGRATION_1_2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
