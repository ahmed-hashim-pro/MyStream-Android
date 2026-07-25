package com.medoapps.www.onlinequran.athan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
}
