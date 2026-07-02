package com.medoapps.www.onlinequran;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.quran.data.core.QuranInfo;
import com.medoapps.www.onlinequran.data.QuranDataProvider;
import com.medoapps.www.onlinequran.data.QuranDisplayData;
import com.medoapps.www.onlinequran.service.QuranDownloadService;
import com.medoapps.www.onlinequran.service.util.DefaultDownloadReceiver;
import com.medoapps.www.onlinequran.service.util.QuranDownloadNotifier;
import com.medoapps.www.onlinequran.service.util.ServiceIntentHelper;
import com.medoapps.www.onlinequran.ui.PagerActivity;
import com.medoapps.www.onlinequran.ui.TranslationManagerActivity;
import com.medoapps.www.onlinequran.util.QuranFileUtils;
import com.medoapps.www.onlinequran.util.QuranUtils;

import javax.inject.Inject;

/**
 * Activity for searching the Quran
 */
public class SearchActivity extends AppCompatActivity
    implements DefaultDownloadReceiver.SimpleDownloadListener,
    LoaderManager.LoaderCallbacks<Cursor> {

  public static final String SEARCH_INFO_DOWNLOAD_KEY = "SEARCH_INFO_DOWNLOAD_KEY";
  private static final String EXTRA_QUERY = "EXTRA_QUERY";

  private TextView messageView;
  private TextView warningView;
  private View warningContainer;
  private Button buttonGetTranslations;
  private View emptyView;
  private TextView emptyDesc;
  private android.widget.SearchView heroSearch;
  private boolean isArabicSearch;
  private String query;
  private ResultAdapter adapter;
  private DefaultDownloadReceiver downloadReceiver;

  @Inject QuranInfo quranInfo;
  @Inject QuranDisplayData quranDisplayData;
  @Inject QuranFileUtils quranFileUtils;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((QuranApplication) getApplication())
        .getApplicationComponent().inject(this);
    setContentView(R.layout.search);
    applyNavyStatusBar();
    messageView = findViewById(R.id.search_area);
    warningView = findViewById(R.id.search_warning);
    warningContainer = findViewById(R.id.search_warning_container);
    emptyView = findViewById(R.id.search_empty);
    emptyDesc = findViewById(R.id.empty_desc);
    buttonGetTranslations = findViewById(R.id.btnGetTranslations);
    buttonGetTranslations.setOnClickListener(v -> {
      startActivity(new Intent(getApplicationContext(), TranslationManagerActivity.class));
      finish();
    });
    // The Arabic-search-DB download lives inside the warning notice (mockup 09),
    // so it stays actionable even while translation results are listed below.
    Button getArabicDb = findViewById(R.id.btnGetArabicSearchDb);
    getArabicDb.setOnClickListener(v -> downloadArabicSearchDb());

    ImageButton back = findViewById(R.id.search_back);
    if (back != null) back.setOnClickListener(v -> finish());
    setupHeroSearch();

    handleIntent(getIntent());
  }

  /** Navy status bar with light icons, matching the rest of the Mushaf re-theme. */
  private void applyNavyStatusBar() {
    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.navy_700));
    WindowInsetsControllerCompat wic =
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
    if (wic != null) wic.setAppearanceLightStatusBars(false);
  }

  /** The hero search field is the real search (SearchableInfo), styled for navy. */
  private void setupHeroSearch() {
    heroSearch = findViewById(R.id.hero_search);
    if (heroSearch == null) return;
    heroSearch.setIconifiedByDefault(false);
    heroSearch.setQueryHint(getString(R.string.search_hint));
    SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    heroSearch.setSearchableInfo(sm.getSearchableInfo(getComponentName()));

    int white = ContextCompat.getColor(this, R.color.text_on_navy);
    int hint = ContextCompat.getColor(this, R.color.hint_on_navy);
    int gold = ContextCompat.getColor(this, R.color.gold_accent);
    EditText q = heroSearch.findViewById(
        getResources().getIdentifier("android:id/search_src_text", null, null));
    if (q != null) { q.setTextColor(white); q.setHintTextColor(hint); }
    View plate = heroSearch.findViewById(
        getResources().getIdentifier("android:id/search_plate", null, null));
    if (plate != null) plate.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    for (String n : new String[]{"search_go_btn", "search_button"}) {
      ImageView ic = heroSearch.findViewById(getResources().getIdentifier("android:id/" + n, null, null));
      if (ic != null) ic.setColorFilter(gold);
    }
    // The clear "✕" is dim white in the mockup — only the lens/actions are colored.
    ImageView close = heroSearch.findViewById(
        getResources().getIdentifier("android:id/search_close_btn", null, null));
    if (close != null) close.setColorFilter(hint);
    // Lens = the mockup's emoji magnifier (multi-color); use its own colors.
    ImageView mag = heroSearch.findViewById(
        getResources().getIdentifier("android:id/search_mag_icon", null, null));
    if (mag != null) { mag.setImageResource(R.drawable.ic_search_lens); mag.clearColorFilter(); }
  }

  // Search lives in the navy hero (setupHeroSearch), not in an action-bar menu.

  @Override
  public void onPause() {
    if (downloadReceiver != null) {
      downloadReceiver.setListener(null);
      LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadReceiver);
      downloadReceiver = null;
    }
    super.onPause();
  }

  private void downloadArabicSearchDb() {
    if (downloadReceiver == null) {
      downloadReceiver = new DefaultDownloadReceiver(this,
          QuranDownloadService.DOWNLOAD_TYPE_ARABIC_SEARCH_DB);
      LocalBroadcastManager.getInstance(this).registerReceiver(
          downloadReceiver, new IntentFilter(QuranDownloadNotifier.ProgressIntent.INTENT_NAME));
    }
    downloadReceiver.setListener(this);

    String url = quranFileUtils.getArabicSearchDatabaseUrl();
    String notificationTitle = getString(R.string.search_data);
    Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
        quranFileUtils.getQuranDatabaseDirectory(this),
        notificationTitle, SEARCH_INFO_DOWNLOAD_KEY,
        QuranDownloadService.DOWNLOAD_TYPE_ARABIC_SEARCH_DB);
    final String extension = url.endsWith(".zip") ? ".zip" : "";
    intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
        QuranDataProvider.QURAN_ARABIC_DATABASE + extension);
    startService(intent);
  }

  @Override
  public void handleDownloadSuccess() {
    warningContainer.setVisibility(View.GONE);
    buttonGetTranslations.setVisibility(View.GONE);
    handleIntent(getIntent());
  }

  @Override
  public void handleDownloadFailure(int errId) {
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String query = args.getString(EXTRA_QUERY);
    this.query = query;
    return new CursorLoader(this, QuranDataProvider.SEARCH_URI,
        null, null, new String[]{ query }, null);
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
    final boolean containsArabic = QuranUtils.doesStringContainArabic(query);
    isArabicSearch = containsArabic;
    @SuppressLint("WrongThread") boolean showArabicWarning = (isArabicSearch &&
        !quranFileUtils.hasArabicSearchDatabase());

    if (showArabicWarning) {
      // overridden because if we search Arabic tafaseer, this tells us to go
      // to the tafseer page instead of the Arabic page when we open the result.
      isArabicSearch = false;

      warningView.setText(getString(R.string.no_arabic_search_available));
      warningContainer.setVisibility(View.VISIBLE);
    } else {
      warningContainer.setVisibility(View.GONE);
    }

    if (cursor == null || cursor.getCount() == 0) {
      // No results: show the themed empty state, hide the count band + list.
      showEmptyState(true);
      // cursor is null either when the query length is less than 3 characters or when
      // there are no valid databases to search at all. in this case, if it's not an
      // Arabic search, show the "get translations" button.
      if (!containsArabic && query != null && query.length() > 2) {
        buttonGetTranslations.setVisibility(View.VISIBLE);
      } else {
        buttonGetTranslations.setVisibility(View.GONE);
      }
      if (adapter != null) {
        adapter.swapCursor(null);
      }
    } else {
      showEmptyState(false);
      // Display the number of results
      int count = cursor.getCount();
      String countString = getResources().getQuantityString(
          R.plurals.search_results, count, query, count);
      messageView.setText(countString);

      ListView listView = findViewById(R.id.results_list);
      if (adapter == null) {
        adapter = new ResultAdapter(this, cursor, quranDisplayData, quranInfo);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
          ListView p = (ListView) parent;
          final Cursor currentCursor = (Cursor) p.getAdapter().getItem(position);
          jumpToResult(currentCursor.getInt(1), currentCursor.getInt(2));
        });
      } else {
        adapter.swapCursor(cursor);
      }
    }
  }

  /** Toggle between the themed empty state and the results list + count band. */
  private void showEmptyState(boolean empty) {
    if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    findViewById(R.id.results_list).setVisibility(empty ? View.GONE : View.VISIBLE);
    messageView.setVisibility(empty ? View.GONE : View.VISIBLE);
    if (empty && emptyDesc != null) emptyDesc.setText(getString(R.string.search_no_results_hint));
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    if (adapter != null) {
      adapter.swapCursor(null);
    }
  }

  private void handleIntent(Intent intent) {
    if (intent == null) {
      return;
    }
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      if (heroSearch != null && query != null) {
        heroSearch.setQuery(query, false);
        heroSearch.clearFocus();
      }
      showResults(query);
    } else if (intent.getAction() == null) {
      // Opened blank — focus the field so the keyboard appears for a fresh search.
      if (heroSearch != null) heroSearch.requestFocus();
    } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      Uri intentData = intent.getData();
      String query = intent.getStringExtra(SearchManager.USER_QUERY);
      if (query == null) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
          // bug on ics where the above returns null
          // http://code.google.com/p/android/issues/detail?id=22978
          Object q = extras.get(SearchManager.USER_QUERY);
          if (q instanceof SpannableString) {
            query = q.toString();
          }
        }
      }

      if (QuranUtils.doesStringContainArabic(query)) {
        isArabicSearch = true;
      }

      if (isArabicSearch) {
        // if we come from muyassar and don't have arabic db, we set
        // arabic search to false so we jump to the translation.
        if (!quranFileUtils.hasArabicSearchDatabase()) {
          isArabicSearch = false;
        }
      }

      Integer id = null;
      try {
        if (intentData != null) {
          id = intentData.getLastPathSegment() != null ?
              Integer.valueOf(intentData.getLastPathSegment()) : null;
        }
      } catch (NumberFormatException e) {
        // no op
      }

      if (id != null) {
        if (id == -1) {
          showResults(query);
          return;
        }
        int sura = 1;
        int total = id;
        for (int j = 1; j <= 114; j++) {
          int cnt = quranInfo.getNumberOfAyahs(j);
          total -= cnt;
          if (total >= 0)
            sura++;
          else {
            total += cnt;
            break;
          }
        }

        if (total == 0){
          sura--;
          total = quranInfo.getNumberOfAyahs(sura);
        }

        jumpToResult(sura, total);
        finish();
      }
    }
  }

  private void jumpToResult(int sura, int ayah) {
    int page = quranInfo.getPageFromSuraAyah(sura, ayah);
    Intent intent = new Intent(this, PagerActivity.class);
    intent.putExtra(PagerActivity.EXTRA_HIGHLIGHT_SURA, sura);
    intent.putExtra(PagerActivity.EXTRA_HIGHLIGHT_AYAH, ayah);
    if (!isArabicSearch) {
      intent.putExtra(PagerActivity.EXTRA_JUMP_TO_TRANSLATION, true);
    }
    intent.putExtra("page", page);
    startActivity(intent);
  }

  private void showResults(String query) {
    Bundle args = new Bundle();
    args.putString(EXTRA_QUERY, query);
    LoaderManager.getInstance(this).restartLoader(0, args, this);
  }

  private static class ResultAdapter extends CursorAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final QuranInfo quranInfo;
    private final QuranDisplayData quranDisplayData;

    ResultAdapter(Context context, Cursor cursor, QuranDisplayData quranDisplayData, QuranInfo quranInfo) {
      super(context, cursor, 0);
      inflater = LayoutInflater.from(context);
      this.context = context;
      this.quranDisplayData = quranDisplayData;
      this.quranInfo = quranInfo;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      final View view = inflater.inflate(R.layout.search_result, parent, false);
      ViewHolder holder = new ViewHolder();
      holder.text = view.findViewById(R.id.verseText);
      holder.metadata = view.findViewById(R.id.verseLocation);
      view.setTag(holder);
      return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      final ViewHolder holder = (ViewHolder) view.getTag();
      int sura = cursor.getInt(1);
      int ayah = cursor.getInt(2);
      int page = quranInfo.getPageFromSuraAyah(sura, ayah);

      // The DB already wraps each match in <font color=translation_highlight> (now gold).
      String text = cursor.getString(3);
      String suraName = quranDisplayData.getSuraName(this.context, sura, false);
      holder.text.setText(Html.fromHtml(text));
      holder.metadata.setText(this.context.getString(R.string.found_in_sura, suraName, ayah, page));
    }

    static class ViewHolder {
      TextView text;
      TextView metadata;
    }
  }
}
