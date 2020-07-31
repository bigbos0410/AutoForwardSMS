package com.ttk.lab.autoforwardsms;

import android.content.SharedPreferences;

public class PreferenceHelper {

    public static void putBoolean (SharedPreferences mPreferences, String name, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static void putString (SharedPreferences mPreferences, String name, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static void putInt (SharedPreferences mPreferences, String name, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(name, value);
        editor.apply();
    }
}
