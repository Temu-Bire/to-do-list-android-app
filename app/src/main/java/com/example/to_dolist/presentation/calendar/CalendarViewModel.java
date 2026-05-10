package com.example.to_dolist.presentation.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.domain.usecase.GetAllTasksUseCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final LiveData<List<Task>> tasksThisMonth;

    @Inject
    public CalendarViewModel(GetAllTasksUseCase getAllTasksUseCase) {
        tasksThisMonth = Transformations.map(
                getAllTasksUseCase.execute(),
                CalendarViewModel::filterCurrentMonth
        );
    }

    private static List<Task> filterCurrentMonth(List<Task> all) {
        List<Task> out = new ArrayList<>();
        if (all == null) return out;
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        Calendar c = Calendar.getInstance();
        for (Task t : all) {
            c.setTimeInMillis(t.getDueDate());
            if (c.get(Calendar.YEAR) == y && c.get(Calendar.MONTH) == m) {
                out.add(t);
            }
        }
        out.sort(Comparator.comparingLong(Task::getDueDate));
        return out;
    }

    public LiveData<List<Task>> getTasksThisMonth() {
        return tasksThisMonth;
    }
}
