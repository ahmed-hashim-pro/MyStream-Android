package com.medoapps.www.onlinequran.athan;

import android.content.Context;
import android.net.Uri;

import com.medoapps.www.onlinequran.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A selectable athan/iqama sound. Sounds are either bundled in res/raw,
 * downloadable from a URL into the app's files dir, the user's own device
 * ringtone, or silent. The same model backs the athan, Fajr-athan, and iqama
 * pickers.
 */
public final class AthanSound {

    public enum Type { BUNDLED, DOWNLOADABLE, DEVICE, SILENT }

    /** Slot a selection applies to (also the SharedPreferences key suffix). */
    public static final String SLOT_ATHAN = "athan";
    public static final String SLOT_FAJR = "fajr";
    public static final String SLOT_IQAMA = "iqama";

    public final String id;
    public final String displayName;
    public final Type type;
    public final int rawRes;        // BUNDLED
    public final String url;        // DOWNLOADABLE
    public final String attribution; // optional credit line

    private AthanSound(String id, String displayName, Type type, int rawRes,
                       String url, String attribution) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rawRes = rawRes;
        this.url = url;
        this.attribution = attribution;
    }

    static AthanSound bundled(String id, String name, int rawRes, String attribution) {
        return new AthanSound(id, name, Type.BUNDLED, rawRes, null, attribution);
    }

    static AthanSound downloadable(String id, String name, String url) {
        return new AthanSound(id, name, Type.DOWNLOADABLE, 0, url, null);
    }

    static AthanSound device(Context c) {
        return new AthanSound("device", c.getString(R.string.athan_sound_device),
                Type.DEVICE, 0, null, null);
    }

    static AthanSound silent(Context c) {
        return new AthanSound("silent", c.getString(R.string.athan_sound_silent),
                Type.SILENT, 0, null, null);
    }

    // --------------------------------------------------------------- catalog

    /** Default selection id per slot. */
    public static String defaultId(String slot) {
        if (SLOT_IQAMA.equals(slot)) return "device";
        if (SLOT_FAJR.equals(slot)) return "default"; // "same as athan"
        return "voice6"; // bundled azan13 — the default athan
    }

    /** Full athan voice catalog (athan + Fajr slots). */
    public static List<AthanSound> athanCatalog(Context c, boolean includeSameAsAthan) {
        List<AthanSound> list = new ArrayList<>();
        if (includeSameAsAthan) {
            list.add(new AthanSound("default", c.getString(R.string.athan_same_as_athan),
                    Type.BUNDLED, 0, null, null));
        }
        // Bundled (offline-ready) voices. voice6 is the default.
        list.add(bundled("voice6", "Athan — Mansour Al-Zahrani", R.raw.athan6, null));
        list.add(bundled("voice1", "Athan — Mishary Rashid", R.raw.athan1, null));
        // Downloadable voices (public URLs; can be migrated to Firebase Storage).
        list.add(downloadable("voice3", "Athan — Abdul Basit", "https://www.islamcan.com/audio/adhan/azan3.mp3"));
        list.add(downloadable("voice5", "Athan — Al-Husary", "https://www.islamcan.com/audio/adhan/azan5.mp3"));
        list.add(downloadable("voice8", "Athan — Ali Mulla (Makkah)", "https://www.islamcan.com/audio/adhan/azan8.mp3"));
        list.add(downloadable("voice11", "Athan — Madinah", "https://www.islamcan.com/audio/adhan/azan11.mp3"));
        list.add(downloadable("voice15", "Athan — Egypt", "https://www.islamcan.com/audio/adhan/azan15.mp3"));
        list.add(downloadable("voice18", "Athan — Istanbul", "https://www.islamcan.com/audio/adhan/azan18.mp3"));
        list.add(downloadable("voice20", "Athan — Al-Qatami", "https://www.islamcan.com/audio/adhan/azan20.mp3"));
        list.add(device(c));
        list.add(silent(c));
        return list;
    }

    /** Iqama catalog: bundled short call + device tone + downloads + silent. */
    public static List<AthanSound> iqamaCatalog(Context c) {
        List<AthanSound> list = new ArrayList<>();
        list.add(device(c));
        list.add(downloadable("iqama1", "Iqama 1", "https://www.islamcan.com/audio/adhan/azan8.mp3"));
        list.add(downloadable("iqama2", "Iqama 2", "https://www.islamcan.com/audio/adhan/azan5.mp3"));
        list.add(silent(c));
        return list;
    }

    public static List<AthanSound> catalogForSlot(Context c, String slot) {
        if (SLOT_IQAMA.equals(slot)) return iqamaCatalog(c);
        return athanCatalog(c, SLOT_FAJR.equals(slot));
    }

    public static AthanSound byId(List<AthanSound> catalog, String id) {
        for (AthanSound s : catalog) if (s.id.equals(id)) return s;
        return catalog.isEmpty() ? null : catalog.get(0);
    }

    // --------------------------------------------------------------- resolve

    public boolean isDownloaded(Context c) {
        return type != Type.DOWNLOADABLE || localFile(c).exists();
    }

    public File localFile(Context c) {
        File dir = new File(c.getFilesDir(), "athans");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, id + ".mp3");
    }

    /**
     * Playable URI, or null when the sound is silent or a download is missing.
     * DEVICE uses the per-slot saved ringtone (falls back to the alarm tone).
     */
    public Uri resolveUri(Context c, String slot) {
        switch (type) {
            case BUNDLED:
                if (rawRes == 0) return null; // "same as athan" placeholder
                return Uri.parse("android.resource://" + c.getPackageName() + "/" + rawRes);
            case DOWNLOADABLE:
                File f = localFile(c);
                return f.exists() ? Uri.fromFile(f) : null;
            case DEVICE:
                String saved = PrayerSettings.getDeviceSoundUri(c, slot);
                if (saved != null && !saved.isEmpty()) return Uri.parse(saved);
                Uri alarm = android.media.RingtoneManager
                        .getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
                return alarm != null ? alarm : android.media.RingtoneManager
                        .getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
            case SILENT:
            default:
                return null;
        }
    }
}
