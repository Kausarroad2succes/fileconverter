package com.fileconverter.model;

public class ThemeState {
    private static boolean darkModeEnabled = false;

    public static boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public static void setDarkModeEnabled(boolean enabled) {
        darkModeEnabled = enabled;
    }
}