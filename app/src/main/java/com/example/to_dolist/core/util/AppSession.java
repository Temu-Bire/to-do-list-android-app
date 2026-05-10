package com.example.to_dolist.core.util;

/**
 * In-memory unlock flag (cleared when the process dies).
 */
public final class AppSession {

    private static volatile boolean unlocked;

    private AppSession() {}

    public static boolean isUnlocked() {
        return unlocked;
    }

    public static void setUnlocked(boolean value) {
        unlocked = value;
    }

    public static void clearUnlock() {
        unlocked = false;
    }
}
