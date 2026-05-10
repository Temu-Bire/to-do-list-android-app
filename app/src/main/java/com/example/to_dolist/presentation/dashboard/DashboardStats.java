package com.example.to_dolist.presentation.dashboard;

import com.example.to_dolist.domain.model.Task;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Aggregates task metrics for analytics screens.
 */
public final class DashboardStats {

    public final int openTasks;
    public final int completedTasks;
    public final int completedDueThisWeek;
    public final float completionRatePercent;
    public final List<BarEntry> upcomingByDay;
    public final List<String> dayLabels;
    public final List<PieEntry> pieSlices;

    private DashboardStats(int openTasks, int completedTasks, int completedDueThisWeek,
                           float completionRatePercent, List<BarEntry> upcomingByDay,
                           List<String> dayLabels, List<PieEntry> pieSlices) {
        this.openTasks = openTasks;
        this.completedTasks = completedTasks;
        this.completedDueThisWeek = completedDueThisWeek;
        this.completionRatePercent = completionRatePercent;
        this.upcomingByDay = upcomingByDay;
        this.dayLabels = dayLabels;
        this.pieSlices = pieSlices;
    }

    public static DashboardStats fromTasks(List<Task> tasks) {
        if (tasks == null) tasks = new ArrayList<>();

        int open = 0;
        int done = 0;
        for (Task t : tasks) {
            if (t.isCompleted()) done++;
            else open++;
        }
        int total = open + done;
        float rate = total == 0 ? 0f : (100f * done / total);

        long weekStart = startOfWeekMillis();
        long weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000;
        int doneThisWeek = 0;
        for (Task t : tasks) {
            if (t.isCompleted() && t.getDueDate() >= weekStart && t.getDueDate() < weekEnd) {
                doneThisWeek++;
            }
        }

        long day = 24L * 60 * 60 * 1000;
        long startToday = startOfDayMillis();
        List<BarEntry> bars = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            long dayStart = startToday + i * day;
            long dayEnd = dayStart + day;
            int count = 0;
            for (Task t : tasks) {
                if (!t.isCompleted() && t.getDueDate() >= dayStart && t.getDueDate() < dayEnd) {
                    count++;
                }
            }
            bars.add(new BarEntry(i, count));
            labels.add(shortWeekday(dayStart));
        }

        List<PieEntry> pie = new ArrayList<>();
        if (open > 0) pie.add(new PieEntry(open, "Open"));
        if (done > 0) pie.add(new PieEntry(done, "Done"));

        return new DashboardStats(open, done, doneThisWeek, rate, bars, labels, pie);
    }

    private static long startOfDayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long startOfWeekMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        return c.getTimeInMillis();
    }

    private static String shortWeekday(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
    }
}
