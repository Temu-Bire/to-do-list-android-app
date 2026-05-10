package com.example.to_dolist.core.util;

import android.content.Context;

import com.example.to_dolist.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private static final SimpleDateFormat DAY_FORMAT =
            new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());

    public static String formatDate(long timestamp) {
        return DISPLAY_FORMAT.format(new Date(timestamp));
    }

    public static String formatDayDate(long timestamp) {
        return DAY_FORMAT.format(new Date(timestamp));
    }

    public static boolean isToday(long timestamp) {
        String today = DISPLAY_FORMAT.format(new Date());
        return today.equals(DISPLAY_FORMAT.format(new Date(timestamp)));
    }

    public static boolean isTomorrow(long timestamp) {
        long tomorrowMs = System.currentTimeMillis() + 86_400_000L;
        String tomorrow = DISPLAY_FORMAT.format(new Date(tomorrowMs));
        return tomorrow.equals(DISPLAY_FORMAT.format(new Date(timestamp)));
    }

    /** Uses the current locale for date formatting (English, Amharic, etc.). */
    public static String toFriendlyLabel(Context context, long timestamp) {
        if (isToday(timestamp)) return context.getString(R.string.due_today);
        if (isTomorrow(timestamp)) return context.getString(R.string.due_tomorrow);
        return formatDate(timestamp);
    }

    /** Milliseconds until 1 hour before the given dueDate. Returns 0 if already past. */
    public static long millisUntilReminderBefore(long dueDate, long leadTimeMs) {
        long reminderAt = dueDate - leadTimeMs;
        long now = System.currentTimeMillis();
        return Math.max(0, reminderAt - now);
    }
}
