package com.medoapps.www.onlinequran.athan;

import java.util.Locale;

/**
 * Maps a user's location to sensible prayer-calculation defaults.
 *
 * Pure and Android-free so it can be unit-tested directly. The geocoded ISO-3166
 * alpha-2 country code is the primary signal; {@link #forCoordinates} is a coarse
 * offline fallback for when reverse geocoding is unavailable (it needs network).
 *
 * Values are {@code com.batoulapps.adhan} enum constant NAMES — see
 * {@link PrayerSettings#setCalculationMethod} / {@link PrayerSettings#setMadhab}.
 */
public final class PrayerLocaleDefaults {

    /** A calculation method + madhab pair for a location. */
    public static final class Defaults {
        public final String method;
        public final String madhab;

        public Defaults(String method, String madhab) {
            this.method = method;
            this.madhab = madhab;
        }
    }

    private static final String MWL = "MUSLIM_WORLD_LEAGUE";
    private static final String SHAFI = "SHAFI";
    private static final String HANAFI = "HANAFI";

    private PrayerLocaleDefaults() {
    }

    /**
     * Defaults for an ISO-3166 alpha-2 country code (case-insensitive, trimmed).
     * Unknown or blank codes fall back to Muslim World League + Shafi.
     */
    public static Defaults forCountry(String iso2) {
        if (iso2 == null || iso2.trim().isEmpty()) {
            return new Defaults(MWL, SHAFI);
        }
        String c = iso2.trim().toUpperCase(Locale.US);
        return new Defaults(methodFor(c), madhabFor(c));
    }

    private static String methodFor(String c) {
        switch (c) {
            case "SA":
                return "UMM_AL_QURA";
            case "AE": case "OM": case "BH":
                return "DUBAI";
            case "KW":
                return "KUWAIT";
            case "QA":
                return "QATAR";
            case "EG": case "LY": case "DZ": case "TN": case "MA":
            case "SD": case "SY": case "IQ": case "JO": case "LB": case "YE":
                return "EGYPTIAN";
            case "PK": case "IN": case "BD": case "AF": case "LK":
                return "KARACHI";
            case "SG": case "MY": case "ID": case "BN":
                return "SINGAPORE";
            case "US": case "CA": case "MX":
                return "NORTH_AMERICA";
            default:
                // Turkey and Europe land here: no dedicated method, MWL is the norm.
                return MWL;
        }
    }

    private static String madhabFor(String c) {
        switch (c) {
            case "PK": case "IN": case "BD": case "AF":
            case "TR": case "UZ": case "TJ": case "TM": case "KZ": case "KG":
                return HANAFI;
            default:
                return SHAFI;
        }
    }

    /**
     * Coarse offline fallback, used only when reverse geocoding gives no country.
     *
     * Boxes are tested in this exact order and the first match wins. The order is
     * load-bearing: the boxes overlap, and the narrow Gulf box must be tested before
     * the wide Saudi one or Dubai (lng 55.3) would be swallowed by it.
     *
     * Deliberately coarse — this only needs to beat a blind UMM_AL_QURA when there is
     * no network. Known limits: Turkey resolves MWL+Shafi here (the country path gets
     * Hanafi right), and Dammam falls in the Gulf box rather than Saudi.
     */
    public static Defaults forCoordinates(double lat, double lng) {
        if (in(lat, 22, 27) && in(lng, 50, 60)) return new Defaults("DUBAI", SHAFI);
        if (in(lat, 16, 32) && in(lng, 34, 56)) return new Defaults("UMM_AL_QURA", SHAFI);
        if (in(lat, 5, 38) && in(lng, 60, 92)) return new Defaults("KARACHI", HANAFI);
        if (in(lat, 20, 38) && in(lng, -18, 36)) return new Defaults("EGYPTIAN", SHAFI);
        if (in(lat, -11, 8) && in(lng, 94, 142)) return new Defaults("SINGAPORE", SHAFI);
        if (in(lat, 15, 72) && in(lng, -170, -50)) return new Defaults("NORTH_AMERICA", SHAFI);
        return new Defaults(MWL, SHAFI);
    }

    private static boolean in(double v, double min, double max) {
        return v >= min && v <= max;
    }

    /**
     * Whether an automatic pick should be (re)applied: true only when the newly
     * resolved key is usable AND differs from the one last applied. This is what
     * stops routine fixes in the user's home country rewriting their settings, and
     * stops a failed resolution wiping a good saved pick.
     */
    public static boolean shouldReapply(String lastApplied, String resolved) {
        if (resolved == null || resolved.trim().isEmpty()) {
            return false;
        }
        String previous = lastApplied == null ? "" : lastApplied.trim();
        return !resolved.trim().equalsIgnoreCase(previous);
    }

    /**
     * Marks a remembered key as coming from the coarse coordinate boxes rather than a real
     * geocoded country. Both kinds share one pref, so they must stay distinguishable: an
     * un-namespaced coordinate key would alternate with the country key for the same place
     * (e.g. "JO" online vs "UMM_AL_QURA" offline in Amman) and every alternation would
     * rewrite the method, flipping the user's prayer times with network availability.
     */
    public static final String COORD_PREFIX = "coord:";

    /** What the auto-pick should write, plus the key to remember it by. */
    public static final class Resolution {
        public final Defaults defaults;
        public final String key;

        Resolution(Defaults defaults, String key) {
            this.defaults = defaults;
            this.key = key;
        }
    }

    /** True when a real geocoded country has already been recorded. */
    public static boolean isCountryKey(String storedKey) {
        return storedKey != null
                && !storedKey.trim().isEmpty()
                && !storedKey.trim().startsWith(COORD_PREFIX);
    }

    /**
     * Decide what the automatic pick should write for a fix, or {@code null} to leave the
     * settings alone.
     *
     * A geocoded country is strictly better information than a coarse box, so once one has
     * been recorded a network-less fix must never downgrade it — that guard is what keeps
     * the method from flapping as connectivity comes and goes. The boxes only serve a cold
     * start, before any country is known.
     *
     * @param storedKey   the key last applied ({@code ""} when nothing has been)
     * @param countryCode ISO-3166 alpha-2 from reverse geocoding; null/empty when offline
     */
    public static Resolution resolve(String storedKey, String countryCode,
                                     double lat, double lng) {
        final boolean geocoded = countryCode != null && !countryCode.trim().isEmpty();

        final Defaults defaults;
        final String key;
        if (geocoded) {
            defaults = forCountry(countryCode);
            key = countryCode.trim().toUpperCase(Locale.US);
        } else {
            if (isCountryKey(storedKey)) {
                return null;
            }
            defaults = forCoordinates(lat, lng);
            key = COORD_PREFIX + defaults.method;
        }

        return shouldReapply(storedKey, key) ? new Resolution(defaults, key) : null;
    }
}
