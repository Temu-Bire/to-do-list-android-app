package com.example.to_dolist.core.di;

import android.content.Context;

import com.example.to_dolist.data.local.dao.CategoryDao;
import com.example.to_dolist.data.local.dao.SubtaskDao;
import com.example.to_dolist.data.local.dao.TaskDao;
import com.example.to_dolist.data.local.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public TaskDao provideTaskDao(AppDatabase database) {
        return database.taskDao();
    }

    @Provides
    public CategoryDao provideCategoryDao(AppDatabase database) {
        return database.categoryDao();
    }

    @Provides
    public SubtaskDao provideSubtaskDao(AppDatabase database) {
        return database.subtaskDao();
    }
}
