/*
Copyright 2016-2018 Jan Henzel, Patrick Jauernig, Dennis Werner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.secuso.privacyfriendlywifimanager.logic.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * This class is used to represent a settings entry in the settings fragment.
 * It also wraps shared preferences for easier access.
 */
public abstract class AbstractSettingsEntry {
    public final static String PREF_SETTINGS = "SHARED_PREF_SETTINGS";
    public final static String PREF_ENTRY_FIRST_RUN = "SHARED_PREF_FIRST_RUN";
    public final static String PREF_ENTRY_SERVICE_ACTIVE = "SHARED_PREF_ENTRY_SERVICE_ACTIVE";
    public final static String PREF_ENTRY_USE_SIGNAL_STRENGTH = "SHARED_PREF_ENTRY_USE_SIGNAL_STRENGTH";
    public final static String PREF_ENTRY_SHOW_NOTIFICATION = "SHARED_PREF_SHOW_NOTIFICATION";
    public final static String PREF_ENTRY_DEVELOPER = "SHARED_PREF_DEVELOPER";

    public String name;
    public String desc;

    public AbstractSettingsEntry(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    /**
     * Getter for {@code PREF_ENTRY_USE_SIGNAL_STRENGTH}
     *
     * @param context A context to use.
     * @return True, if signal strength should be respected in calculations.
     */
    public static boolean shouldRespectSignalStrength(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        return settings.getBoolean(PREF_ENTRY_USE_SIGNAL_STRENGTH, true);
    }

    /**
     * Getter for {@code PREF_ENTRY_USE_SIGNAL_STRENGTH}
     *
     * @return True, if signal strength should be respected in calculations.
     */
    public static boolean shouldRespectSignalStrength() {
        return shouldRespectSignalStrength(StaticContext.getContext());
    }

    /**
     * Setter for {@code PREF_ENTRY_SERVICE_ACTIVE}
     *
     * @param context A context to use.
     * @param state   True, if the flag should be set.
     */
    public static void setActiveFlag(Context context, boolean state) {
        SharedPreferences settings = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        settings.edit().putBoolean(PREF_ENTRY_SERVICE_ACTIVE, state).apply();
    }

    /**
     * Setter for {@code PREF_ENTRY_SERVICE_ACTIVE}
     *
     * @param state True, if the flag should be set.
     */
    public static void setActiveFlag(boolean state) {
        setActiveFlag(StaticContext.getContext(), state);
    }

    /**
     * Getter for {@code PREF_ENTRY_SERVICE_ACTIVE}
     *
     * @param context A context to use.
     * @return True, if service is active.
     */
    public static boolean isServiceActive(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);

        return settings.getBoolean(PREF_ENTRY_SERVICE_ACTIVE, false);
    }

    /**
     * Getter for {@code PREF_ENTRY_SERVICE_ACTIVE}
     *
     * @return True, if service is active.
     */
    public static boolean isServiceActive() {
        return isServiceActive(StaticContext.getContext());
    }

    /**
     * Checks if ACCESS_COARSE_LOCATION has been granted.
     *
     * @param context A context to use.
     * @return True, if ACCESS_COARSE_LOCATION has been granted, false otherwise
     */
    public static boolean hasCoarseLocationPermission(Context context) {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        return ContextCompat.checkSelfPermission(context, permission) == (int) PackageManager.PERMISSION_GRANTED;
    }
}
