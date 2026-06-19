package com.medoapps.www.onlinequran.athan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The Suhur (fasting) reminder must only fire during Ramadan (hijri month 9),
 * even when the reminder is enabled. {@link HijriDate#isRamadan(Calendar)} is the
 * gate used by FastingReminderReceiver; these dates are anchored to the app's own
 * tabular conversion.
 */
public class HijriDateRamadanTest {

    /** Calendar months are 0-based; accept a natural 1-12 month here. */
    private static Calendar greg(int year, int month1to12, int day) {
        return new GregorianCalendar(year, month1to12 - 1, day);
    }

    @Test
    public void ramadanDaysAreDetected() {
        assertTrue(HijriDate.isRamadan(greg(2026, 2, 19))); // 1447-09-02
        assertTrue(HijriDate.isRamadan(greg(2026, 3, 1)));  // 1447-09-12
        assertTrue(HijriDate.isRamadan(greg(2026, 3, 10))); // 1447-09-21
    }

    @Test
    public void nonRamadanDaysAreNotDetected() {
        assertFalse(HijriDate.isRamadan(greg(2026, 2, 10))); // 1447-08-22 (Sha'ban)
        assertFalse(HijriDate.isRamadan(greg(2026, 3, 25))); // 1447-10-06 (Shawwal / Eid)
        assertFalse(HijriDate.isRamadan(greg(2026, 6, 19))); // 1448-01-03 (today)
        assertFalse(HijriDate.isRamadan(greg(2025, 9, 15))); // 1447-03-22
    }
}
