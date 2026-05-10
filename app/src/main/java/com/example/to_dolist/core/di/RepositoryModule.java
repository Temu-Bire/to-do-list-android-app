package com.example.to_dolist.core.di;

import com.example.to_dolist.data.repository.TaskRepositoryImpl;
import com.example.to_dolist.domain.repository.ITaskRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract ITaskRepository bindTaskRepository(TaskRepositoryImpl taskRepositoryImpl);
}
