package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import com.medoapps.www.onlinequran.data.Constants;

import java.util.Locale;

/**
 * App language (UI locale) controller. Three choices — {@link #SYSTEM} / {@link #ARABIC} /
 * {@link #ENGLISH} — applied via AndroidX per-app locales ({@link AppCompatDelegate#setApplicationLocales}),
 * which recreates activities and persists the choice (see the AppLocalesMetadataHolderService in the
 * manifest for pre-Android-13 storage).
 *
 * It also keeps the legacy {@code useArabicNames} flag — which the mushaf/lists/number formatting key
 * off — in sync with the effective locale, so the lists always follow the app language.
 */
public final class AppLanguage {

    public static final String SYSTEM = "system";
    public static final String ARABIC = "ar";
    public static final String ENGLISH = "en";

    private AppLanguage() {
    }

    /**
     * The active choice — derived from the live per-app locale ({@link AppCompatDelegate#getApplicationLocales})
     * so it always reflects what is really applied, even if the locale was changed outside the in-app
     * picker (e.g. Android's per-app language setting). Returns {@link #SYSTEM} / {@link #ARABIC} / {@link #ENGLISH}.
     */
    public static String getCurrent(Context context) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return SYSTEM;
        }
        String lang = locales.get(0).getLanguage();
        if (ARABIC.equals(lang)) return ARABIC;
        if (ENGLISH.equals(lang)) return ENGLISH;
        return SYSTEM;
    }

    /** Applies a language choice app-wide and keeps the lists/RTL flag in sync. */
    public static void apply(Context context, String lang) {
        LocaleListCompat locales = SYSTEM.equals(lang)
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(locales);
        syncArabicNames(context);
    }

    /**
     * Keeps {@link Constants#PREF_USE_ARABIC_NAMES} aligned with the effective UI locale so the
     * sura/reciter lists, RTL and number formatting follow the app language. Safe to call on every
     * app start.
     */
    public static void syncArabicNames(Context context) {
        boolean isArabic = ARABIC.equals(effectiveLocale().getLanguage());
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(Constants.PREF_USE_ARABIC_NAMES, isArabic)
                // Legacy flag (1=Arabic, 2=Latin) that the menu/radio/reciter list classes read for
                // fonts and a few literals; keep it aligned with the app language.
                .putInt("LanguageSelect", isArabic ? 1 : 2)
                .apply();
        SettingSaved.LanguageSelect = isArabic ? 1 : 2;
    }

    /** Human label for the current choice (for a settings subtitle). */
    public static String currentLabel(Context context) {
        String cur = getCurrent(context);
        int res = ARABIC.equals(cur) ? R.string.language_arabic
                : ENGLISH.equals(cur) ? R.string.language_english
                : R.string.language_system;
        return context.getString(res);
    }

    /**
     * Shows the styled language chooser (System / العربية / English). {@code firstRun} = the welcome
     * ask (non-cancelable). Picking a row applies it (recreating the activity).
     */
    public static void showPicker(android.app.Activity activity, boolean firstRun) {
        android.view.View view = activity.getLayoutInflater().inflate(R.layout.dialog_language, null);
        ((android.widget.TextView) view.findViewById(R.id.lang_title))
                .setText(firstRun ? R.string.onb_language_title : R.string.settings_language);

        final androidx.appcompat.app.AlertDialog dialog =
                // pinned overlay: the Settings screen swaps alertDialogTheme for a
                // Material3 one that collapses this custom-view dialog's window
                new androidx.appcompat.app.AlertDialog.Builder(
                        activity, R.style.ThemeOverlay_MyStream_LangDialog)
                        .setView(view)
                        .setCancelable(!firstRun)
                        .create();

        String cur = getCurrent(activity);
        markSelected(view, R.id.opt_system, R.id.check_system, SYSTEM.equals(cur));
        markSelected(view, R.id.opt_arabic, R.id.check_arabic, ARABIC.equals(cur));
        markSelected(view, R.id.opt_english, R.id.check_english, ENGLISH.equals(cur));

        view.findViewById(R.id.opt_system).setOnClickListener(v -> {
            dialog.dismiss();
            apply(activity, SYSTEM);
        });
        view.findViewById(R.id.opt_arabic).setOnClickListener(v -> {
            dialog.dismiss();
            apply(activity, ARABIC);
        });
        view.findViewById(R.id.opt_english).setOnClickListener(v -> {
            dialog.dismiss();
            apply(activity, ENGLISH);
        });

        dialog.show();
    }

    private static void markSelected(android.view.View root, int rowId, int checkId, boolean selected) {
        root.findViewById(rowId).setActivated(selected);
        root.findViewById(checkId).setVisibility(
                selected ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
    }

    /**
     * Whether the effective UI locale is right-to-left. Driven by the per-app/system locale rather
     * than {@code Locale.getDefault()} (which {@code setApplicationLocales(empty)} leaves stale on a
     * live process), so it stays correct for System mode after a runtime switch.
     */
    public static boolean isRtl(Context context) {
        return android.text.TextUtils.getLayoutDirectionFromLocale(effectiveLocale())
                == android.view.View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * A context whose resources resolve in the effective app locale. Legacy list/menu classes build
     * titles with {@code context.getString(...)} against a context that may still carry the system
     * locale; wrap that context with this so those lists follow the app language.
     */
    public static Context localizedContext(Context base) {
        android.content.res.Configuration cfg =
                new android.content.res.Configuration(base.getResources().getConfiguration());
        cfg.setLocale(effectiveLocale());
        return base.createConfigurationContext(cfg);
    }

    /** The locale currently in effect: the per-app locale if set, otherwise the system locale. */
    private static Locale effectiveLocale() {
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        if (!appLocales.isEmpty()) {
            return appLocales.get(0);
        }
        Locale system = ConfigurationCompat.getLocales(
                Resources.getSystem().getConfiguration()).get(0);
        return system != null ? system : Locale.getDefault();
    }
}
