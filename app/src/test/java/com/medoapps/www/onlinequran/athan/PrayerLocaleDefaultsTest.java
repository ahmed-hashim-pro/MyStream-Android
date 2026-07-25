package com.medoapps.www.onlinequran.athan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PrayerLocaleDefaultsTest {

    @Test
    public void forCountry_mapsTheGulf() {
        assertEquals("UMM_AL_QURA", PrayerLocaleDefaults.forCountry("SA").method);
        assertEquals("DUBAI", PrayerLocaleDefaults.forCountry("AE").method);
        assertEquals("KUWAIT", PrayerLocaleDefaults.forCountry("KW").method);
        assertEquals("QATAR", PrayerLocaleDefaults.forCountry("QA").method);
    }

    @Test
    public void forCountry_mapsTheMajorRegions() {
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCountry("EG").method);
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry("PK").method);
        assertEquals("SINGAPORE", PrayerLocaleDefaults.forCountry("ID").method);
        assertEquals("NORTH_AMERICA", PrayerLocaleDefaults.forCountry("US").method);
    }

    @Test
    public void forCountry_isCaseInsensitiveAndTrims() {
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry("pk").method);
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry(" PK ").method);
    }

    @Test
    public void forCountry_unknownOrBlankFallsBackToMwlShafi() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("ZZ").method);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("ZZ").madhab);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry(null).method);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("").method);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("   ").method);
    }

    @Test
    public void forCountry_picksHanafiForSouthAndCentralAsia() {
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("PK").madhab);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("IN").madhab);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("TR").madhab);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("EG").madhab);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("SA").madhab);
    }

    /** Turkey has no dedicated method but must still get the Hanafi madhab. */
    @Test
    public void forCountry_turkeyIsMwlButHanafi() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("TR").method);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("TR").madhab);
    }

    /** Box order is load-bearing: the narrow Gulf box must beat the wide Saudi one. */
    @Test
    public void forCoordinates_resolvesRealCities() {
        assertEquals("UMM_AL_QURA", PrayerLocaleDefaults.forCoordinates(21.4225, 39.8262).method); // Makkah
        assertEquals("DUBAI", PrayerLocaleDefaults.forCoordinates(25.20, 55.27).method);           // Dubai
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCoordinates(30.04, 31.24).method);        // Cairo
        assertEquals("KARACHI", PrayerLocaleDefaults.forCoordinates(24.86, 67.01).method);         // Karachi
        assertEquals("SINGAPORE", PrayerLocaleDefaults.forCoordinates(-6.21, 106.85).method);      // Jakarta
        assertEquals("NORTH_AMERICA", PrayerLocaleDefaults.forCoordinates(43.65, -79.38).method);  // Toronto
    }

    @Test
    public void forCoordinates_unmappedFallsBackToMwl() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCoordinates(51.5, -0.12).method); // London
    }

    @Test
    public void forCoordinates_southAsiaIsHanafi() {
        assertEquals("HANAFI", PrayerLocaleDefaults.forCoordinates(24.86, 67.01).madhab);
    }

    @Test
    public void shouldReapply_onlyWhenTheKeyActuallyChanges() {
        assertTrue(PrayerLocaleDefaults.shouldReapply("", "EG"));
        assertTrue(PrayerLocaleDefaults.shouldReapply(null, "EG"));
        assertTrue(PrayerLocaleDefaults.shouldReapply("SA", "EG"));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "EG"));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "eg"));
    }

    /** A blank resolution must never wipe a good saved pick. */
    @Test
    public void shouldReapply_isFalseWhenResolutionIsUnusable() {
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", null));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", ""));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "  "));
    }

    /**
     * Regression: the country path and the coordinate path disagree in real places, so if
     * both wrote a bare key into the same pref the stored value would alternate with network
     * availability and the method would flap. These are the concrete disagreements that made
     * the flap possible — LocationApplier namespaces the coordinate key to prevent it.
     */
    @Test
    public void countryAndCoordinatePathsDisagreeInRealPlaces() {
        // Amman: country JO -> EGYPTIAN, but the coarse Saudi box claims UMM_AL_QURA
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCountry("JO").method);
        assertEquals("UMM_AL_QURA", PrayerLocaleDefaults.forCoordinates(31.95, 35.93).method);

        // Antalya: country TR -> MWL + HANAFI, coordinates fall in the Egyptian box + SHAFI
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("TR").method);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("TR").madhab);
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCoordinates(36.9, 30.7).method);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCoordinates(36.9, 30.7).madhab);
    }

    // ------------------------------------------------------------- resolve()

    @Test
    public void resolve_appliesOnFirstGeocodedFix() {
        PrayerLocaleDefaults.Resolution r = PrayerLocaleDefaults.resolve("", "EG", 30.04, 31.24);
        assertNotNull(r);
        assertEquals("EGYPTIAN", r.defaults.method);
        assertEquals("SHAFI", r.defaults.madhab);
        assertEquals("EG", r.key);
    }

    @Test
    public void resolve_appliesWhenTheCountryChanges() {
        PrayerLocaleDefaults.Resolution r = PrayerLocaleDefaults.resolve("EG", "PK", 24.86, 67.01);
        assertNotNull(r);
        assertEquals("KARACHI", r.defaults.method);
        assertEquals("HANAFI", r.defaults.madhab);
        assertEquals("PK", r.key);
    }

    @Test
    public void resolve_doesNothingWhenTheCountryIsUnchanged() {
        assertNull(PrayerLocaleDefaults.resolve("EG", "EG", 30.04, 31.24));
        assertNull(PrayerLocaleDefaults.resolve("EG", "eg", 30.04, 31.24));
    }

    /** Cold start with no network: the coarse boxes are better than nothing. */
    @Test
    public void resolve_usesCoordinatesWhenNothingIsKnownYet() {
        PrayerLocaleDefaults.Resolution r = PrayerLocaleDefaults.resolve("", null, 24.86, 67.01);
        assertNotNull(r);
        assertEquals("KARACHI", r.defaults.method);
        assertEquals(PrayerLocaleDefaults.COORD_PREFIX + "KARACHI", r.key);
    }

    /**
     * THE FLAP REGRESSION. In Amman the country path says EGYPTIAN and the coarse box says
     * UMM_AL_QURA. Once a real country is known, a network-less fix must be ignored entirely
     * - otherwise the key alternates with connectivity and the method (and in Turkey's case
     * the madhab, worth ~an hour of Asr) flips back and forth forever.
     */
    @Test
    public void resolve_offlineFixNeverDowngradesAKnownCountry() {
        assertNull(PrayerLocaleDefaults.resolve("JO", null, 31.95, 35.93));
        assertNull(PrayerLocaleDefaults.resolve("JO", "", 31.95, 35.93));
        assertNull(PrayerLocaleDefaults.resolve("JO", "   ", 31.95, 35.93));
        // Antalya: the flap would swing the madhab, not just the method
        assertNull(PrayerLocaleDefaults.resolve("TR", null, 36.9, 30.7));
    }

    /** A coord key is provisional: a later real country must still be allowed to win. */
    @Test
    public void resolve_geocodedCountryUpgradesAProvisionalCoordKey() {
        String coordKey = PrayerLocaleDefaults.COORD_PREFIX + "UMM_AL_QURA";
        PrayerLocaleDefaults.Resolution r =
                PrayerLocaleDefaults.resolve(coordKey, "JO", 31.95, 35.93);
        assertNotNull(r);
        assertEquals("EGYPTIAN", r.defaults.method);
        assertEquals("JO", r.key);
    }

    /** Two offline fixes in the same coarse region must only write once. */
    @Test
    public void resolve_offlineIsStableWithinTheSameRegion() {
        String coordKey = PrayerLocaleDefaults.COORD_PREFIX + "KARACHI";
        assertNull(PrayerLocaleDefaults.resolve(coordKey, null, 24.86, 67.01));
    }

    @Test
    public void isCountryKey_distinguishesTheTwoNamespaces() {
        assertTrue(PrayerLocaleDefaults.isCountryKey("EG"));
        assertFalse(PrayerLocaleDefaults.isCountryKey(PrayerLocaleDefaults.COORD_PREFIX + "EGYPTIAN"));
        assertFalse(PrayerLocaleDefaults.isCountryKey(""));
        assertFalse(PrayerLocaleDefaults.isCountryKey(null));
    }
}
