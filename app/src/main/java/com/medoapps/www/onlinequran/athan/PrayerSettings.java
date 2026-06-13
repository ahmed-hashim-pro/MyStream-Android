package com.medoapps.www.onlinequran.athan;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Central settings store for the athan feature, backed by SharedPreferences.
 *
 * Prayer indices used across the whole athan package (matches the order the
 * widget cache uses): 0 Fajr, 1 Sunrise, 2 Dhuhr, 3 Asr, 4 Maghrib, 5 Isha.
 */
public final class PrayerSettings {

    public static final String PREFS = "athan_settings";

    public static final int PRAYER_FAJR = 0;
    public static final int PRAYER_SUNRISE = 1;
    public static final int PRAYER_DHUHR = 2;
    public static final int PRAYER_ASR = 3;
    public static final int PRAYER_MAGHRIB = 4;
    public static final int PRAYER_ISHA = 5;
    public static final int PRAYER_COUNT = 6;

    /** Per-prayer notification modes. */
    public static final int MODE_OFF = 0;
    public static final int MODE_SILENT = 1;
    public static final int MODE_BEEP = 2;
    public static final int MODE_ATHAN = 3;

    /** Location modes. */
    public static final int LOCATION_AUTO = 0;
    public static final int LOCATION_MANUAL = 1;

    private PrayerSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------------- method

    /** Name of a com.batoulapps.adhan.CalculationMethod enum constant. */
    public static String getCalculationMethod(Context c) {
        return prefs(c).getString("calculation_method", "UMM_AL_QURA");
    }

    public static void setCalculationMethod(Context c, String enumName) {
        prefs(c).edit().putString("calculation_method", enumName).apply();
    }

    /** "SHAFI" or "HANAFI" (com.batoulapps.adhan.Madhab). */
    public static String getMadhab(Context c) {
        return prefs(c).getString("madhab", "SHAFI");
    }

    public static void setMadhab(Context c, String enumName) {
        prefs(c).edit().putString("madhab", enumName).apply();
    }

    /** com.batoulapps.adhan.HighLatitudeRule enum constant name. */
    public static String getHighLatitudeRule(Context c) {
        return prefs(c).getString("high_latitude_rule", "MIDDLE_OF_THE_NIGHT");
    }

    public static void setHighLatitudeRule(Context c, String enumName) {
        prefs(c).edit().putString("high_latitude_rule", enumName).apply();
    }

    // ----------------------------------------------------------- corrections

    /** Manual correction in minutes (-59..59) added to the computed time. */
    public static int getCorrection(Context c, int prayerIndex) {
        return prefs(c).getInt("correction_" + prayerIndex, 0);
    }

    public static void setCorrection(Context c, int prayerIndex, int minutes) {
        prefs(c).edit().putInt("correction_" + prayerIndex, minutes).apply();
    }

    // ----------------------------------------------------- notification mode

    /** Default: full athan for the five prayers, off for sunrise. */
    public static int getNotificationMode(Context c, int prayerIndex) {
        int def = (prayerIndex == PRAYER_SUNRISE) ? MODE_OFF : MODE_ATHAN;
        return prefs(c).getInt("mode_" + prayerIndex, def);
    }

    public static void setNotificationMode(Context c, int prayerIndex, int mode) {
        prefs(c).edit().putInt("mode_" + prayerIndex, mode).apply();
    }

    // -------------------------------------------------------- feature master

    /** Master switch: when off, no athan/reminder alarms or notifications fire. */
    public static boolean isAthanFeatureEnabled(Context c) {
        return prefs(c).getBoolean("athan_feature_enabled", true);
    }

    public static void setAthanFeatureEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("athan_feature_enabled", enabled).apply();
    }

    // ----------------------------------------------------------------- sound

    /**
     * Selected sound id for a slot ({@link AthanSound#SLOT_ATHAN}/SLOT_FAJR/
     * SLOT_IQAMA). See {@link AthanSound#defaultId(String)}.
     */
    public static String getSoundId(Context c, String slot) {
        return prefs(c).getString("sound_id_" + slot, AthanSound.defaultId(slot));
    }

    public static void setSoundId(Context c, String slot, String id) {
        prefs(c).edit().putString("sound_id_" + slot, id).apply();
    }

    /** Per-slot device ringtone URI (used when the slot's sound id is "device"). */
    public static String getDeviceSoundUri(Context c, String slot) {
        return prefs(c).getString("device_uri_" + slot, "");
    }

    public static void setDeviceSoundUri(Context c, String slot, String uri) {
        prefs(c).edit().putString("device_uri_" + slot, uri == null ? "" : uri).apply();
    }

    /**
     * The sound slot a given prayer's athan should use: Fajr uses the Fajr
     * slot unless it's set to "same as athan".
     */
    public static String athanSlotForPrayer(Context c, int prayerIndex) {
        if (prayerIndex == PRAYER_FAJR
                && !"default".equals(getSoundId(c, AthanSound.SLOT_FAJR))) {
            return AthanSound.SLOT_FAJR;
        }
        return AthanSound.SLOT_ATHAN;
    }

    public static boolean isVibrateEnabled(Context c) {
        return prefs(c).getBoolean("vibrate", true);
    }

    public static void setVibrateEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("vibrate", enabled).apply();
    }

    /** Whether to append the post-athan dua to athan notifications. */
    public static boolean isDuaAfterAthanEnabled(Context c) {
        return prefs(c).getBoolean("dua_after_athan", true);
    }

    public static void setDuaAfterAthanEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("dua_after_athan", enabled).apply();
    }

    // ------------------------------------------------------------- reminders

    /** Minutes before each prayer for an advance reminder; 0 = off. */
    public static int getPreReminderMinutes(Context c) {
        return prefs(c).getInt("pre_reminder_minutes", 0);
    }

    public static void setPreReminderMinutes(Context c, int minutes) {
        prefs(c).edit().putInt("pre_reminder_minutes", minutes).apply();
    }

    /** Minutes after the athan for an iqama reminder; 0 = off. */
    public static int getIqamaReminderMinutes(Context c) {
        return prefs(c).getInt("iqama_reminder_minutes", 0);
    }

    public static void setIqamaReminderMinutes(Context c, int minutes) {
        prefs(c).edit().putInt("iqama_reminder_minutes", minutes).apply();
    }

    // -------------------------------------------------------------- location

    public static int getLocationMode(Context c) {
        return prefs(c).getInt("location_mode", LOCATION_AUTO);
    }

    public static void setLocationMode(Context c, int mode) {
        prefs(c).edit().putInt("location_mode", mode).apply();
    }

    public static boolean hasLocation(Context c) {
        return prefs(c).contains("lat");
    }

    public static double getLatitude(Context c) {
        // Makkah fallback when nothing is saved yet
        return Double.longBitsToDouble(prefs(c).getLong("lat", Double.doubleToLongBits(21.4225)));
    }

    public static double getLongitude(Context c) {
        return Double.longBitsToDouble(prefs(c).getLong("lng", Double.doubleToLongBits(39.8262)));
    }

    public static String getCityName(Context c) {
        return prefs(c).getString("city_name", "");
    }

    public static void setLocation(Context c, double lat, double lng, String cityName) {
        prefs(c).edit()
                .putLong("lat", Double.doubleToLongBits(lat))
                .putLong("lng", Double.doubleToLongBits(lng))
                .putString("city_name", cityName == null ? "" : cityName)
                .apply();
    }

    // ----------------------------------------------------------------- hijri

    /** Day offset (-2..2) applied to the computed hijri date. */
    public static int getHijriOffset(Context c) {
        return prefs(c).getInt("hijri_offset", 0);
    }

    public static void setHijriOffset(Context c, int offset) {
        prefs(c).edit().putInt("hijri_offset", offset).apply();
    }
}
