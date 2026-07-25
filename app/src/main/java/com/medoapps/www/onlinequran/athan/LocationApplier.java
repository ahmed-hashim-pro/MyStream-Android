package com.medoapps.www.onlinequran.athan;

import android.content.Context;

import java.util.Locale;

/**
 * The single place a location fix is turned into athan settings.
 *
 * Every screen that obtains a fix — onboarding, {@code PrayerTimesActivity} and
 * {@code AthanSettingsActivity} — funnels through {@link #apply}, so the automatic
 * calculation-method pick behaves identically everywhere, including when the user
 * travels. Adding a fourth fix site means calling this, not re-implementing it.
 */
public final class LocationApplier {

    private LocationApplier() {
    }

    /**
     * Persist a fix and, when auto-update is on and the region actually changed,
     * re-derive the calculation method and madhab from it. Always reschedules.
     *
     * @param city        best-effort locality name; empty string is fine
     * @param countryCode ISO-3166 alpha-2 from reverse geocoding. null/empty is fine —
     *                    a coarse coordinate fallback is used instead, so this still
     *                    works with no network.
     */
    public static void apply(Context context, double lat, double lng,
                             String city, String countryCode) {
        // these writes outlive whatever screen requested the fix
        Context c = context.getApplicationContext();
        PrayerSettings.setLocation(c, lat, lng, city);

        if (PrayerSettings.isAutoMethodEnabled(c)) {
            boolean geocoded = countryCode != null && !countryCode.trim().isEmpty();
            PrayerLocaleDefaults.Defaults defaults = geocoded
                    ? PrayerLocaleDefaults.forCountry(countryCode)
                    : PrayerLocaleDefaults.forCoordinates(lat, lng);
            // Key the did-it-change check on the country when we have one, else on the
            // resolved method — so an offline fix also only writes once per region
            // instead of on every single fix.
            String key = geocoded
                    ? countryCode.trim().toUpperCase(Locale.US)
                    : defaults.method;
            if (PrayerLocaleDefaults.shouldReapply(PrayerSettings.getAutoMethodCountry(c), key)) {
                PrayerSettings.setCalculationMethod(c, defaults.method);
                PrayerSettings.setMadhab(c, defaults.madhab);
                PrayerSettings.setAutoMethodCountry(c, key);
            }
        }

        AthanScheduler.rescheduleAll(c);
    }
}
