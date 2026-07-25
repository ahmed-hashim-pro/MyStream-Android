package com.medoapps.www.onlinequran.more;

import android.app.Activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Collections;
import java.util.List;

/**
 * Immutable render state for the More page. See design/specs/more-page.md sec.9.
 *
 * The fragment renders this and nothing else — it never reads settings or prayer
 * data directly. Everything the screen shows is decided in {@link MoreViewModel}.
 */
public final class MoreUiState {

    /** Null while the context card is still being computed (spec: loading). */
    @Nullable public final ContextCard contextCard;
    /** Already filtered by {@link #query}; groups with no matches are absent. */
    public final List<Group> groups;
    /** "" when not searching. */
    public final String query;
    public final boolean searchActive;

    public MoreUiState(@Nullable ContextCard contextCard, List<Group> groups,
                       String query, boolean searchActive) {
        this.contextCard = contextCard;
        this.groups = Collections.unmodifiableList(groups);
        this.query = query == null ? "" : query;
        this.searchActive = searchActive;
    }

    /** True when a search is running and matched nothing (spec: empty-search state). */
    public boolean isSearchEmpty() {
        return !query.isEmpty() && groups.isEmpty();
    }

    // ------------------------------------------------------------------ card

    /** The next-prayer card at position 0. */
    public static final class ContextCard {
        public final String prayerName;
        public final String timeText;
        public final String cityText;
        public final String countdownText;
        /** True → render the "Set your location" variant instead (spec: error / permission). */
        public final boolean needsLocation;

        public ContextCard(String prayerName, String timeText, String cityText,
                           String countdownText, boolean needsLocation) {
            this.prayerName = prayerName;
            this.timeText = timeText;
            this.cityText = cityText;
            this.countdownText = countdownText;
            this.needsLocation = needsLocation;
        }
    }

    // ---------------------------------------------------------------- groups

    public static final class Group {
        @StringRes public final int titleRes;
        public final List<Entry> entries;

        public Group(@StringRes int titleRes, List<Entry> entries) {
            this.titleRes = titleRes;
            this.entries = Collections.unmodifiableList(entries);
        }
    }

    /** A row carries live state; a tile does not. That rule is the whole design. */
    public enum Shape { ROW, TILE }

    public static final class Entry {
        /** Stable across rebuilds — DiffUtil and analytics key on this, not position. */
        public final String id;
        @StringRes public final int titleRes;
        @DrawableRes public final int iconRes;
        public final Shape shape;
        @Nullable public final String subtitle;
        @Nullable public final String state;
        @Nullable public final Class<? extends Activity> destination;
        /** Live Streaming swaps in a fragment instead of launching an activity. */
        public final boolean opensLiveList;

        public Entry(String id, @StringRes int titleRes, @DrawableRes int iconRes, Shape shape,
                     @Nullable String subtitle, @Nullable String state,
                     @Nullable Class<? extends Activity> destination, boolean opensLiveList) {
            this.id = id;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.shape = shape;
            this.subtitle = subtitle;
            this.state = state;
            this.destination = destination;
            this.opensLiveList = opensLiveList;
        }
    }
}
