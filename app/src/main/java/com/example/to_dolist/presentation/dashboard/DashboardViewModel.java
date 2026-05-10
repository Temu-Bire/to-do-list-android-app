package com.example.to_dolist.presentation.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.usecase.GetAllTasksUseCase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final LiveData<DashboardStats> stats;
    private final LiveData<List<Task>> upcomingTasks;

    @Inject
    public DashboardViewModel(GetAllTasksUseCase getAllTasksUseCase) {
        stats = Transformations.map(
                getAllTasksUseCase.execute(),
                DashboardStats::fromTasks
        );
        upcomingTasks = Transformations.map(getAllTasksUseCase.execute(), tasks -> {
            List<Task> u = new ArrayList<>();
            long now = System.currentTimeMillis();
            if (tasks == null) return u;
            for (Task t : tasks) {
                if (!t.isCompleted() && t.getDueDate() >= now) {
                    u.add(t);
                }
            }
            u.sort(Comparator.comparingLong(Task::getDueDate));
            return u.size() > 10 ? new ArrayList<>(u.subList(0, 10)) : u;
        });
    }

    public LiveData<DashboardStats> getStats() {
        return stats;
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        return upcomingTasks;
    }
}
