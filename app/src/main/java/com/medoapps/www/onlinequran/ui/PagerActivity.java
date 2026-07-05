package com.medoapps.www.onlinequran.ui;

import static com.medoapps.www.onlinequran.ui.helpers.SlidingPagerAdapter.AUDIO_PAGE;
import static com.medoapps.www.onlinequran.ui.helpers.SlidingPagerAdapter.TAG_PAGE;
import static com.medoapps.www.onlinequran.ui.helpers.SlidingPagerAdapter.TRANSLATION_PAGE;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.app.Dialog;
import androidx.appcompat.app.AppCompatActivity;

import com.medoapps.www.onlinequran.util.AppBottomSheet;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.NonRestoringViewPager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.quran.data.core.QuranInfo;
import com.quran.data.model.SuraAyah;
import com.quran.data.model.selection.AyahSelection;
import com.quran.data.model.selection.AyahSelectionKt;
import com.quran.data.model.selection.SelectionIndicator;
import com.quran.data.model.selection.SelectionIndicatorKt;
import com.quran.data.page.provider.di.QuranPageExtrasComponent;
import com.quran.data.page.provider.di.QuranPageExtrasComponentProvider;
import com.medoapps.www.onlinequran.HelpActivity;
import com.medoapps.www.onlinequran.QuranApplication;
import com.medoapps.www.onlinequran.QuranPreferenceActivity;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.SearchActivity;
import com.medoapps.www.onlinequran.bridge.AudioEventPresenterBridge;
import com.medoapps.www.onlinequran.bridge.ReadingEventPresenterBridge;
import com.medoapps.www.onlinequran.common.LocalTranslation;
import com.medoapps.www.onlinequran.common.LocalTranslationDisplaySort;
import com.medoapps.www.onlinequran.common.QuranAyahInfo;
import com.medoapps.www.onlinequran.common.audio.QariItem;
import com.medoapps.www.onlinequran.dao.audio.AudioRequest;
import com.medoapps.www.onlinequran.data.Constants;
import com.medoapps.www.onlinequran.data.QuranDataProvider;
import com.medoapps.www.onlinequran.data.QuranDisplayData;
import com.medoapps.www.onlinequran.database.TranslationsDBAdapter;
import com.medoapps.www.onlinequran.di.component.activity.PagerActivityComponent;
import com.medoapps.www.onlinequran.di.module.activity.PagerActivityModule;
import com.medoapps.www.onlinequran.di.module.fragment.QuranPageModule;
import com.medoapps.www.onlinequran.model.bookmark.BookmarkModel;
import com.medoapps.www.onlinequran.model.translation.ArabicDatabaseUtils;
import com.medoapps.www.onlinequran.presenter.audio.AudioPresenter;
import com.medoapps.www.onlinequran.presenter.bookmark.RecentPagePresenter;
import com.medoapps.www.onlinequran.presenter.data.QuranEventLogger;
import com.medoapps.www.onlinequran.presenter.recitation.PagerActivityRecitationPresenter;
import com.medoapps.www.onlinequran.service.AudioService;
import com.medoapps.www.onlinequran.service.QuranDownloadService;
import com.medoapps.www.onlinequran.service.util.DefaultDownloadReceiver;
import com.medoapps.www.onlinequran.service.util.QuranDownloadNotifier;
import com.medoapps.www.onlinequran.service.util.ServiceIntentHelper;
import com.medoapps.www.onlinequran.ui.fragment.AddTagDialog;
import com.medoapps.www.onlinequran.ui.fragment.JumpFragment;
import com.medoapps.www.onlinequran.ui.fragment.TabletFragment;
import com.medoapps.www.onlinequran.ui.fragment.TagBookmarkDialog;
import com.medoapps.www.onlinequran.ui.fragment.TranslationFragment;
import com.medoapps.www.onlinequran.ui.helpers.AyahSelectedListener;
import com.medoapps.www.onlinequran.ui.helpers.AyahTracker;
import com.medoapps.www.onlinequran.ui.helpers.JumpDestination;
import com.medoapps.www.onlinequran.ui.helpers.QuranDisplayHelper;
import com.medoapps.www.onlinequran.ui.helpers.QuranPage;
import com.medoapps.www.onlinequran.ui.helpers.QuranPageAdapter;
import com.medoapps.www.onlinequran.ui.helpers.SlidingPagerAdapter;
import com.medoapps.www.onlinequran.ui.util.ToastCompat;
import com.medoapps.www.onlinequran.ui.util.TranslationsSpinnerAdapter;
import com.medoapps.www.onlinequran.util.AudioUtils;
import com.medoapps.www.onlinequran.util.QuranAppUtils;
import com.medoapps.www.onlinequran.util.QuranFileUtils;
import com.medoapps.www.onlinequran.util.QuranScreenInfo;
import com.medoapps.www.onlinequran.util.QuranSettings;
import com.medoapps.www.onlinequran.util.QuranUtils;
import com.medoapps.www.onlinequran.util.ShareUtil;
import com.medoapps.www.onlinequran.view.AudioStatusBar;
import com.medoapps.www.onlinequran.view.IconPageIndicator;
import com.medoapps.www.onlinequran.view.QuranSpinner;
import com.medoapps.www.onlinequran.view.SlidingUpPanelLayout;
import com.quran.mobile.di.AyahActionFragmentProvider;
import com.quran.mobile.di.QuranReadingActivityComponent;
import com.quran.mobile.di.QuranReadingActivityComponentProvider;
import com.quran.page.common.factory.PageViewFactoryProvider;
import com.quran.page.common.toolbar.AyahToolBar;
import com.quran.page.common.toolbar.di.AyahToolBarInjector;
import com.quran.reading.common.AudioEventPresenter;
import com.quran.reading.common.ReadingEventPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Activity that displays the Quran (in Arabic or translation mode).
 * <p>
 * Essentially, this activity consists of a {@link ViewPager} of Quran pages (using {@link
 * QuranPageAdapter}). {@link AudioService} is used to handle playing audio, and this is synced with
 * the display of the Quran.
 */
public class PagerActivity extends AppCompatActivity implements
    AudioStatusBar.AudioBarListener,
    DefaultDownloadReceiver.DownloadListener,
    TagBookmarkDialog.OnBookmarkTagsUpdateListener,
    AyahSelectedListener,
    JumpDestination,
    QuranReadingActivityComponentProvider,
    QuranPageExtrasComponentProvider,
    AyahToolBarInjector,
    ActivityCompat.OnRequestPermissionsResultCallback {
  private static final String AUDIO_DOWNLOAD_KEY = "AUDIO_DOWNLOAD_KEY";
  private static final String LAST_READ_PAGE = "LAST_READ_PAGE";
  /** Persistent last-read page for the home "continue reading" card. */
  public static final String HOME_LAST_READ_PAGE = "home_last_read_page";
  /** Total pages of the page set the last-read page belongs to. */
  public static final String HOME_LAST_READ_TOTAL = "home_last_read_total";
  private static final String LAST_READING_MODE_IS_TRANSLATION =
      "LAST_READING_MODE_IS_TRANSLATION";
  private static final String LAST_ACTIONBAR_STATE = "LAST_ACTIONBAR_STATE";
  private static final String LAST_AUDIO_REQUEST = "LAST_AUDIO_REQUEST";
  private static final String STATE_AUTO_PLAY_CONSUMED = "autoPlayConsumed";

  public static final String EXTRA_JUMP_TO_TRANSLATION = "jumpToTranslation";
  public static final String EXTRA_HIGHLIGHT_SURA = "highlightSura";
  public static final String EXTRA_HIGHLIGHT_AYAH = "highlightAyah";
  /** When true, the reader auto-starts recitation once open (Home "read+listen"). */
  public static final String EXTRA_AUTO_PLAY = "autoPlay";
  public static final String LAST_WAS_DUAL_PAGES = "wasDualPages";

  private static final long DEFAULT_HIDE_AFTER_TIME = 2000;

  private long lastPopupTime = 0;
  private boolean isActionBarHidden = true;
  private AudioStatusBar audioStatusBar = null;
  /** One-shot guards for the Home "read + listen" auto-start. */
  private boolean autoPlayConsumed = false;
  private boolean forceStreamOnce = false;
  private ViewPager viewPager = null;
  private QuranPageAdapter pagerAdapter = null;
  private boolean shouldReconnect = false;
  private SparseBooleanArray bookmarksCache = null;
  private boolean showingTranslation = false;
  private DefaultDownloadReceiver downloadReceiver;
  private boolean needsPermissionToDownloadOver3g = true;
  private boolean isFocusMode = false;
  private Dialog promptDialog = null;
  private AyahToolBar ayahToolBar;
  private AudioRequest lastAudioRequest;
  private boolean isDualPages = false;
  private View toolBarArea;
  private Toolbar toolbar;
  private View readingModeBar;
  private View sepiaOverlay;
  private TextView readingModeDay;
  private TextView readingModeSepia;
  private TextView readingModeNight;
  private boolean promptedForExtraDownload;
  private QuranSpinner translationsSpinner;
  private View translationSourceStrip;
  private TextView translationSourceName;
  private ProgressDialog progressDialog;
  private ViewGroup.MarginLayoutParams audioBarParams;
  private boolean isInMultiWindowMode;

  private MenuItem bookmarksMenuItem;

  private String[] translationNames;
  private List<LocalTranslation> translations;
  private Set<String> activeTranslationsFilesNames;
  private TranslationsSpinnerAdapter translationsSpinnerAdapter;

  public static final int MSG_HIDE_ACTIONBAR = 1;

  // AYAH ACTION PANEL STUFF
  // Max height of sliding panel (% of screen)
  private static final float PANEL_MAX_HEIGHT = 0.6f;
  private SlidingUpPanelLayout slidingPanel;
  private ViewPager slidingPager;
  private SlidingPagerAdapter slidingPagerAdapter;

  private int numberOfPages;
  private int numberOfPagesDual;
  private int defaultNavigationBarColor;
  private boolean isSplitScreen = false;

  @Nullable private QuranAyahInfo lastSelectedTranslationAyah;
  @Nullable private LocalTranslation[] lastActivatedLocalTranslations;

  private PagerActivityComponent pagerActivityComponent;

  @Inject BookmarkModel bookmarkModel;
  @Inject RecentPagePresenter recentPagePresenter;
  @Inject QuranSettings quranSettings;
  @Inject QuranScreenInfo quranScreenInfo;
  @Inject ArabicDatabaseUtils arabicDatabaseUtils;
  @Inject TranslationsDBAdapter translationsDBAdapter;
  @Inject QuranAppUtils quranAppUtils;
  @Inject ShareUtil shareUtil;
  @Inject AudioUtils audioUtils;
  @Inject QuranDisplayData quranDisplayData;
  @Inject QuranInfo quranInfo;
  @Inject QuranFileUtils quranFileUtils;
  @Inject AudioPresenter audioPresenter;
  @Inject QuranEventLogger quranEventLogger;
  @Inject AudioEventPresenter audioEventPresenter;
  @Inject ReadingEventPresenter readingEventPresenter;
  @Inject PageViewFactoryProvider pageProviderFactoryProvider;
  @Inject Set<AyahActionFragmentProvider> additionalAyahPanels;
  @Inject PagerActivityRecitationPresenter pagerActivityRecitationPresenter;

  private AudioEventPresenterBridge audioEventPresenterBridge;
  private ReadingEventPresenterBridge readingEventPresenterBridge;

  private CompositeDisposable compositeDisposable;
  private final CompositeDisposable foregroundDisposable = new CompositeDisposable();

  private final PagerHandler handler = new PagerHandler(this);

  private static class PagerHandler extends Handler {
    private final WeakReference<PagerActivity> activity;

    PagerHandler(PagerActivity activity) {
      this.activity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      PagerActivity activity = this.activity.get();
      if (activity != null) {
        if (msg.what == MSG_HIDE_ACTIONBAR) {
          activity.toggleActionBarVisibility(false);
        } else {
          super.handleMessage(msg);
        }
      }
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    QuranApplication quranApp = (QuranApplication) getApplication();
    quranApp.refreshLocale(this, false);
    super.onCreate(savedInstanceState);

    // field injection
    getPagerActivityComponent().inject(this);

    bookmarksCache = new SparseBooleanArray();

    boolean shouldAdjustPageNumber = false;
    isDualPages = QuranUtils.isDualPages(this, quranScreenInfo);
    isSplitScreen = quranSettings.isQuranSplitWithTranslation();
    audioEventPresenterBridge = new AudioEventPresenterBridge(
        audioEventPresenter,
        suraAyah -> { onAudioPlaybackAyahChanged(suraAyah); return null; }
    );
    readingEventPresenterBridge = new ReadingEventPresenterBridge(
        readingEventPresenter,
        () -> { onPageClicked(); return null; },
        ayahSelection -> { onAyahSelectionChanged(ayahSelection); return null; }
    );

    // remove the window background to avoid overdraw. note that, per Romain's blog, this is
    // acceptable (as long as we don't set the background color to null in the theme, since
    // that is used to generate preview windows).
    getWindow().setBackgroundDrawable(null);

    numberOfPages = quranInfo.getNumberOfPages();
    numberOfPagesDual = quranInfo.getNumberOfPagesDual();

    int page = -1;
    isActionBarHidden = true;
    if (savedInstanceState != null) {
      Timber.d("non-null saved instance state!");
      page = savedInstanceState.getInt(LAST_READ_PAGE, -1);
      if (page != -1) {
        page = numberOfPages - page;
      }
      showingTranslation = savedInstanceState
          .getBoolean(LAST_READING_MODE_IS_TRANSLATION, false);
      if (savedInstanceState.containsKey(LAST_ACTIONBAR_STATE)) {
        isActionBarHidden = !savedInstanceState.getBoolean(LAST_ACTIONBAR_STATE);
      }
      boolean lastWasDualPages = savedInstanceState.getBoolean(LAST_WAS_DUAL_PAGES, isDualPages);
      shouldAdjustPageNumber = (lastWasDualPages != isDualPages);
      this.lastAudioRequest = savedInstanceState.getParcelable(LAST_AUDIO_REQUEST);
      // Don't re-trigger the Home read+listen auto-start after a recreate.
      autoPlayConsumed = savedInstanceState.getBoolean(STATE_AUTO_PLAY_CONSUMED, false);
    } else {
      Intent intent = getIntent();
      Bundle extras = intent.getExtras();
      if (extras != null) {
        page = numberOfPages - extras.getInt("page", Constants.PAGES_FIRST);
        showingTranslation = extras.getBoolean(EXTRA_JUMP_TO_TRANSLATION, showingTranslation);
        final int highlightedSura = extras.getInt(EXTRA_HIGHLIGHT_SURA, -1);
        final int highlightedAyah = extras.getInt(EXTRA_HIGHLIGHT_AYAH, -1);

        if (highlightedSura > -1 && highlightedAyah > -1) {
          readingEventPresenterBridge.setSelection(highlightedSura, highlightedAyah, true);
        }
      }
    }

    compositeDisposable = new CompositeDisposable();

    setContentView(R.layout.quran_page_activity_slider);
    audioStatusBar = findViewById(R.id.audio_area);
    audioStatusBar.setIsDualPageMode(quranScreenInfo.isDualPageMode());
    audioStatusBar.setQariList(audioUtils.getQariList(this));
    audioStatusBar.setAudioBarListener(this);
    audioBarParams = (ViewGroup.MarginLayoutParams) audioStatusBar.getLayoutParams();

    toolBarArea = findViewById(R.id.toolbar_area);
    translationsSpinner = findViewById(R.id.spinner);

    // Text-mode strip: font A−/A+ + translation-source chip
    translationSourceStrip = findViewById(R.id.translation_source_strip);
    translationSourceName = findViewById(R.id.translation_source_name);
    findViewById(R.id.translation_font_increase)
        .setOnClickListener(v -> changeTranslationFontSize(2));
    findViewById(R.id.translation_font_decrease)
        .setOnClickListener(v -> changeTranslationFontSize(-2));
    findViewById(R.id.translation_source_chip)
        .setOnClickListener(v -> startTranslationManager());

    // Day / Sepia / Night reading-mode switcher
    readingModeBar = findViewById(R.id.reading_mode_bar);
    sepiaOverlay = findViewById(R.id.reading_sepia_overlay);
    readingModeDay = findViewById(R.id.reading_mode_day);
    readingModeSepia = findViewById(R.id.reading_mode_sepia);
    readingModeNight = findViewById(R.id.reading_mode_night);
    readingModeDay.setOnClickListener(v -> setReadingMode(false, false));
    readingModeSepia.setOnClickListener(v -> setReadingMode(false, true));
    readingModeNight.setOnClickListener(v -> setReadingMode(true, false));
    applyReadingMode();

    // this is the colored view behind the status bar on kitkat and above
    final View statusBarBackground = findViewById(R.id.status_bg);
    statusBarBackground.getLayoutParams().height = getStatusBarHeight();

    toolbar = findViewById(R.id.toolbar);
    if (quranSettings.isArabicNames() || QuranUtils.isRtl()) {
      // remove when we remove LTR from quran_page_activity's root
      ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_RTL);
    }
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowHomeEnabled(true);
      ab.setDisplayHomeAsUpEnabled(true);
    }

    initAyahActionPanel();

    if (showingTranslation && translationNames != null) {
      updateActionBarSpinner();
      updateTranslationSourceStrip();
    } else {
      updateActionBarTitle(numberOfPages - page);
    }

    lastPopupTime = System.currentTimeMillis();
    pagerAdapter = new QuranPageAdapter(
        getSupportFragmentManager(),
        isDualPages,
        showingTranslation,
        quranInfo,
        isSplitScreen,
        pageProviderFactoryProvider.providePageViewFactory(quranSettings.getPageType())
    );
    ayahToolBar = findViewById(R.id.ayah_toolbar);
    ayahToolBar.setPageType(quranSettings.getPageType());
    ayahToolBar.setLongPressLambda(charSequence -> {
      ToastCompat.makeText(PagerActivity.this, charSequence, Toast.LENGTH_SHORT).show();
      return null;
    });

    final NonRestoringViewPager nonRestoringViewPager = findViewById(R.id.quran_pager);
    nonRestoringViewPager.setIsDualPagesInLandscape(
        QuranUtils.isDualPagesInLandscape(this, quranScreenInfo));

    viewPager = nonRestoringViewPager;
    viewPager.setAdapter(pagerAdapter);

    ayahToolBar.setOnItemSelectedListener(new AyahMenuItemSelectionHandler());
    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

      @Override
      public void onPageScrollStateChanged(int state) {
      }

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        final AyahSelection currentSelection = readingEventPresenter.currentAyahSelection();
        final SelectionIndicator selectionIndicator =
            AyahSelectionKt.selectionIndicator(currentSelection);
        final SuraAyah suraAyah = AyahSelectionKt.startSuraAyah(currentSelection);
        if (selectionIndicator != SelectionIndicator.None.INSTANCE &&
            selectionIndicator != SelectionIndicator.ScrollOnly.INSTANCE &&
            suraAyah != null) {
          final int startPage = quranInfo.getPageFromSuraAyah(suraAyah.sura, suraAyah.ayah);
          int barPos = quranInfo.getPositionFromPage(startPage, isDualPageVisible());
          if (position == barPos) {
            // Swiping to next ViewPager page (i.e. prev quran page)
            final SelectionIndicator updatedSelectionIndicator =
                SelectionIndicatorKt.withXScroll(selectionIndicator, -positionOffsetPixels);
            readingEventPresenterBridge.withSelectionIndicator(updatedSelectionIndicator);
          } else if (position == barPos - 1 || position == barPos + 1) {
            // Swiping to previous or next ViewPager page (i.e. next or previous quran page)
            final SelectionIndicator updatedSelectionIndicator =
                SelectionIndicatorKt.withXScroll(selectionIndicator, viewPager.getWidth() - positionOffsetPixels);
            readingEventPresenterBridge.withSelectionIndicator(updatedSelectionIndicator);
          } else {
            readingEventPresenterBridge.clearSelectedAyah();
          }
        }
      }

      @Override
      public void onPageSelected(int position) {
        Timber.d("onPageSelected(): %d", position);
        final int page = quranInfo.getPageFromPosition(position, isDualPageVisible());
        if (quranSettings.shouldDisplayMarkerPopup()) {
          lastPopupTime = QuranDisplayHelper.displayMarkerPopup(
              PagerActivity.this, quranInfo, page, lastPopupTime);
          if (isDualPages) {
            lastPopupTime = QuranDisplayHelper.displayMarkerPopup(
                PagerActivity.this, quranInfo, page - 1, lastPopupTime);
          }
        }

        if (!showingTranslation) {
          updateActionBarTitle(page);
        } else {
          refreshActionBarSpinner();
        }

        if (bookmarksCache.indexOfKey(page) < 0) {
          if (isDualPageVisible() && bookmarksCache.indexOfKey(page - 1) < 0) {
            checkIfPageIsBookmarked(page - 1, page);
          } else {
            // we don't have the key
            checkIfPageIsBookmarked(page);
          }
        } else {
          refreshBookmarksMenu();
        }

        // If we're more than 1 page away from ayah selection end ayah mode
        final SuraAyah suraAyah = getSelectionStart();
        if (suraAyah != null) {
          final int startPage = quranInfo.getPageFromSuraAyah(suraAyah.sura, suraAyah.ayah);
          int ayahPos = quranInfo.getPositionFromPage(startPage, isDualPageVisible());
          if (Math.abs(ayahPos - position) > 1) {
            endAyahMode();
          }
        }
      }
    };
    viewPager.addOnPageChangeListener(onPageChangeListener);

    setUiVisibilityListener();
    audioStatusBar.setVisibility(View.VISIBLE);
    toggleActionBarVisibility(true);

    if (shouldAdjustPageNumber) {
      // when going from two page per screen to one or vice versa, we adjust the page number,
      // such that the first page is always selected.
      int curPage = numberOfPages - page;
      if (isDualPageVisible()) {
        if (curPage % 2 != 0) {
          curPage++;
        }
        curPage = numberOfPagesDual - (curPage / 2);
      } else {
        if (curPage % 2 == 0) {
          curPage--;
        }
        curPage = numberOfPages - curPage;
      }
      page = curPage;
    } else if (isDualPageVisible()) {
      page = page / 2;
    }

    viewPager.setCurrentItem(page);
    if (page == 0) {
      onPageChangeListener.onPageSelected(0);
    }

    // just got created, need to reconnect to service
    shouldReconnect = true;

    // enforce orientation lock
    if (quranSettings.isLockOrientation()) {
      int current = getResources().getConfiguration().orientation;
      if (quranSettings.isLandscapeOrientation()) {
        if (current == Configuration.ORIENTATION_PORTRAIT) {
          setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
          return;
        }
      } else if (current == Configuration.ORIENTATION_LANDSCAPE) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return;
      }
    }

    LocalBroadcastManager.getInstance(this).registerReceiver(
        audioReceiver,
        new IntentFilter(AudioService.AudioUpdateIntent.INTENT_NAME));

    downloadReceiver = new DefaultDownloadReceiver(this,
        QuranDownloadService.DOWNLOAD_TYPE_AUDIO);
    String action = QuranDownloadNotifier.ProgressIntent.INTENT_NAME;
    LocalBroadcastManager.getInstance(this).registerReceiver(
        downloadReceiver,
        new IntentFilter(action));
    downloadReceiver.setListener(this);

    defaultNavigationBarColor = getWindow().getNavigationBarColor();

    quranEventLogger.logAnalytics(isDualPages, showingTranslation, isSplitScreen);

    // Setup recitation (if enabled)
    pagerActivityRecitationPresenter.bind(this, new PagerActivityRecitationPresenter.Bridge(
        this::isDualPageVisible,
        this::getCurrentPage,
        () -> audioStatusBar,
        () -> ayahToolBar,
        ayah -> { ensurePage(ayah.sura, ayah.ayah); return null; },
        sliderPage -> { showSlider(slidingPagerAdapter.getPagePosition(sliderPage)); return null; }
    ));
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    pagerActivityRecitationPresenter.onPermissionsResult(requestCode, grantResults);
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private boolean isDualPageVisible() {
    return isDualPages && !(isSplitScreen && showingTranslation);
  }

  private boolean shouldUpdatePageNumber() {
    return isDualPages && isSplitScreen;
  }

  public Observable<Integer> getViewPagerObservable() {
    return Observable.create(e -> {
      final OnPageChangeListener pageChangedListener =
          new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
              e.onNext(quranInfo.getPageFromPosition(position, isDualPageVisible()));
            }
          };

      viewPager.addOnPageChangeListener(pageChangedListener);
      e.onNext(getCurrentPage());

      e.setCancellable(() -> viewPager.removeOnPageChangeListener(pageChangedListener));
    });
  }

  private int getStatusBarHeight() {
    // thanks to https://github.com/jgilfelt/SystemBarTint for this
    final Resources resources = getResources();
    final int resId = resources.getIdentifier(
        "status_bar_height", "dimen", "android");
    if (resId > 0) {
      return resources.getDimensionPixelSize(resId);
    }
    return 0;
  }

  private void initAyahActionPanel() {
    slidingPanel = findViewById(R.id.sliding_panel);
    final ViewGroup slidingLayout =
        slidingPanel.findViewById(R.id.sliding_layout);
    slidingPager = slidingPanel
        .findViewById(R.id.sliding_layout_pager);
    final IconPageIndicator slidingPageIndicator =
        slidingPanel
            .findViewById(R.id.sliding_pager_indicator);

    // Find close button and set listener
    final View closeButton = slidingPanel
        .findViewById(R.id.sliding_menu_close);
    closeButton.setOnClickListener(v -> endAyahMode());

    // Create and set fragment pager adapter
    slidingPagerAdapter = new SlidingPagerAdapter(getSupportFragmentManager(),
        quranSettings.isArabicNames() || QuranUtils.isRtl(),
        additionalAyahPanels);
    slidingPager.setAdapter(slidingPagerAdapter);

    // Attach the view pager to the action bar
    slidingPageIndicator.setViewPager(slidingPager);

    // Set sliding layout parameters
    int displayHeight = getResources().getDisplayMetrics().heightPixels;
    slidingLayout.getLayoutParams().height =
        (int) (displayHeight * PANEL_MAX_HEIGHT);
    slidingPanel.setEnableDragViewTouchEvents(true);
    slidingPanel.setPanelSlideListener(new SlidingPanelListener());
    slidingLayout.setVisibility(View.GONE);

    // When clicking any menu items, expand the panel
    slidingPageIndicator.setOnClickListener(v -> {
      if (!slidingPanel.isExpanded()) {
        slidingPanel.expandPane();
      }
    });
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      handler.sendEmptyMessageDelayed(MSG_HIDE_ACTIONBAR, DEFAULT_HIDE_AFTER_TIME);
    } else {
      handler.removeMessages(MSG_HIDE_ACTIONBAR);
    }
  }

  public void onPageClicked() {
    if (isFocusMode) return; // Don't toggle UI in focus mode
    toggleActionBar();
  }

  private void onAudioPlaybackAyahChanged(@Nullable SuraAyah suraAyah) {
    if (suraAyah != null) {
      // continue to snap back to the page when the playback ayah changes
      ensurePage(suraAyah.sura, suraAyah.ayah);
      // reciter-deck readout on the audio bar: "Surah · ayah N" (mockup 18 B)
      audioStatusBar.setNowPlayingInfo(getString(R.string.audiobar_now_playing,
          quranDisplayData.getSuraName(this, suraAyah.sura, false),
          QuranUtils.getLocalizedNumber(this, suraAyah.ayah)));
    }
  }

  private void onAyahSelectionChanged(AyahSelection ayahSelection) {
    final boolean haveSelection = ayahSelection != AyahSelection.None.INSTANCE;
    final SelectionIndicator currentSelection = AyahSelectionKt.selectionIndicator(ayahSelection);
    if (currentSelection instanceof SelectionIndicator.None && haveSelection) {
      viewPager.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    if (haveSelection) {
      final SuraAyah startPosition = startPosition(ayahSelection);
      updateLocalTranslations(startPosition);
    } else {
      endAyahMode();
    }
  }

  private SuraAyah startPosition(AyahSelection ayahSelection) {
    if (ayahSelection instanceof AyahSelection.Ayah) {
      return ((AyahSelection.Ayah) ayahSelection).getSuraAyah();
    } else if (ayahSelection instanceof AyahSelection.AyahRange) {
      return ((AyahSelection.AyahRange) ayahSelection).getStartSuraAyah();
    } else {
      return null;
    }
  }

  private void setUiVisibility(boolean isVisible) {
    setUiVisibilityKitKat(isVisible);
    if (isInMultiWindowMode) {
      animateToolBar(isVisible);
    }
  }

  private void setUiVisibilityKitKat(boolean isVisible) {
    int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    if (!isVisible) {
      flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_IMMERSIVE;
    }
    viewPager.setSystemUiVisibility(flags);
  }

  private void setUiVisibilityListener() {
    viewPager.setOnSystemUiVisibilityChangeListener(
        flags -> {
          boolean visible = (flags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
          animateToolBar(visible);
        });
  }

  private void clearUiVisibilityListener() {
    viewPager.setOnSystemUiVisibilityChangeListener(null);
  }

  private void animateToolBar(boolean visible) {
    isActionBarHidden = !visible;
    if (visible) {
      audioStatusBar.updateSelectedItem();
    }

    // animate toolbar
    toolBarArea.animate()
        .translationY(visible ? 0 : -toolBarArea.getHeight())
        .setDuration(250)
        .start();

    // the text-mode strip sits just under the toolbar; slide it off with the chrome
    if (translationSourceStrip != null && showingTranslation) {
      translationSourceStrip.animate()
          .translationY(visible ? 0
              : -(toolBarArea.getHeight() + translationSourceStrip.getHeight()))
          .setDuration(250)
          .start();
    }

    /* the bottom margin on the audio bar is not part of its height, and so we have to
     * take it into account when animating the audio bar off the screen. */
    final int bottomMargin = audioBarParams.bottomMargin;

    // and audio bar
    audioStatusBar.animate()
        .translationY(visible ? 0 : audioStatusBar.getHeight() + bottomMargin)
        .setDuration(250)
        .start();

    // and the reading-mode switcher (slides off below with the chrome)
    if (readingModeBar != null) {
      readingModeBar.animate()
          .translationY(visible ? 0 :
              readingModeBar.getHeight() + audioStatusBar.getHeight() + bottomMargin)
          .alpha(visible ? 1f : 0f)
          .setDuration(250)
          .start();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean navigate = audioStatusBar.getCurrentMode() !=
        AudioStatusBar.PLAYING_MODE
        && quranSettings.navigateWithVolumeKeys();
    if (navigate && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
      return true;
    } else if (navigate && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    return ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
        keyCode == KeyEvent.KEYCODE_VOLUME_UP) &&
        audioStatusBar.getCurrentMode() !=
            AudioStatusBar.PLAYING_MODE &&
        PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(Constants.PREF_USE_VOLUME_KEY_NAV, false))
        || super.onKeyUp(keyCode, event);
  }

  @Override
  public void onResume() {
    super.onResume();

    audioPresenter.bind(this);
    recentPagePresenter.bind(this);
    isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode();

    // read the list of translations
    requestTranslationsList();

    if (shouldReconnect) {
      foregroundDisposable.add(Completable.timer(500, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(() -> {
            startService(
                audioUtils.getAudioIntent(PagerActivity.this, AudioService.ACTION_CONNECT));
            shouldReconnect = false;
          }));
    }

    // Home "read + listen": auto-start recitation once, after the audio service
    // connects. Stream this one-shot so it never blocks on a download dialog.
    if (!autoPlayConsumed
        && getIntent() != null
        && getIntent().getBooleanExtra(EXTRA_AUTO_PLAY, false)) {
      autoPlayConsumed = true;
      // Clear the flag from the launch intent so a later recreate can't re-fire it.
      getIntent().removeExtra(EXTRA_AUTO_PLAY);
      setIntent(getIntent());
      foregroundDisposable.add(Completable.timer(900, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(() -> {
            forceStreamOnce = true;
            onPlayPressed();
          }));
    }

    updateNavigationBar(quranSettings.isNightMode());
  }

  private void updateNavigationBar(boolean isNightMode) {
    final int color =
        isNightMode ? ContextCompat.getColor(this, R.color.navbar_night_color) :
            defaultNavigationBarColor;
    getWindow().setNavigationBarColor(color);
  }

  /** Persist the chosen reading mode (Day/Sepia/Night) and re-render. */
  private void setReadingMode(boolean night, boolean sepia) {
    quranSettings.setReadingMode(night, sepia);
    refreshQuranPages();
    applyReadingMode();
  }

  /** Reflect the current reading mode: sepia overlay, chip selection, nav bar. */
  private void applyReadingMode() {
    if (readingModeBar == null) {
      return;
    }
    final boolean night = quranSettings.isNightMode();
    final boolean sepia = !night && quranSettings.isSepiaMode();
    sepiaOverlay.setVisibility(sepia ? View.VISIBLE : View.GONE);
    styleReadingChip(readingModeNight, night);
    styleReadingChip(readingModeSepia, sepia);
    styleReadingChip(readingModeDay, !night && !sepia);
    updateNavigationBar(night);
  }

  private void styleReadingChip(TextView chip, boolean selected) {
    chip.setBackgroundResource(selected ? R.drawable.bg_reading_chip_on : 0);
    chip.setTextColor(ContextCompat.getColor(
        this, selected ? R.color.navy_900 : R.color.text_secondary));
  }

  @NonNull
  public PagerActivityComponent getPagerActivityComponent() {
    // a fragment may call this before Activity's onCreate, so cache and reuse.
    if (pagerActivityComponent == null) {
      pagerActivityComponent = ((QuranApplication) getApplication())
          .getApplicationComponent()
          .pagerActivityComponentBuilder()
          .withPagerActivityModule(new PagerActivityModule(this))
          .build();
    }
    return pagerActivityComponent;
  }

  @NonNull
  @Override
  public QuranReadingActivityComponent provideQuranReadingActivityComponent() {
    return getPagerActivityComponent();
  }

  @NonNull
  @Override
  public QuranPageExtrasComponent provideQuranPageExtrasComponent(@NonNull int... pages) {
    return getPagerActivityComponent()
        .quranPageComponentBuilder()
        .withQuranPageModule(new QuranPageModule(pages))
        .build();
  }

  @Override
  public void injectToolBar(@NonNull AyahToolBar ayahToolBar) {
    getPagerActivityComponent()
        .inject(ayahToolBar);
  }

  public void showGetRequiredFilesDialog() {
    if (promptDialog != null) {
      return;
    }
    promptDialog = AppBottomSheet.showConfirmation(this,
        "",
        getString(R.string.download_extra_data),
        getString(R.string.downloadPrompt_ok),
        getString(R.string.downloadPrompt_no),
        () -> {
            downloadRequiredFiles();
            promptDialog = null;
        },
        () -> promptDialog = null);
  }

  private void downloadRequiredFiles() {
    int downloadType = QuranDownloadService.DOWNLOAD_TYPE_AUDIO;
    if (audioStatusBar.getCurrentMode() == AudioStatusBar.STOPPED_MODE) {
      // if we're stopped, use audio download bar as our progress bar
      audioStatusBar.switchMode(AudioStatusBar.DOWNLOADING_MODE);
      if (isActionBarHidden) {
        toggleActionBar();
      }
    } else {
      // if audio is playing, let's not disrupt it - do this using a
      // different type so the broadcast receiver ignores it.
      downloadType = QuranDownloadService.DOWNLOAD_TYPE_ARABIC_SEARCH_DB;
    }

    boolean haveDownload = false;
    if (!quranFileUtils.haveAyaPositionFile(this)) {
      String url = quranFileUtils.getAyaPositionFileUrl();
      if (QuranUtils.isDualPages(this, quranScreenInfo)) {
        url = quranFileUtils.getAyaPositionFileUrl(
            quranScreenInfo.getTabletWidthParam());
      }
      String destination = quranFileUtils.getQuranAyahDatabaseDirectory(this);
      // start the download
      String notificationTitle = getString(R.string.highlighting_database);
      Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
          destination, notificationTitle, AUDIO_DOWNLOAD_KEY,
          downloadType);
      Timber.d("starting service to download ayah position file");
      startService(intent);

      haveDownload = true;
    }

    if (!quranFileUtils.hasArabicSearchDatabase()) {
      String url = quranFileUtils.getArabicSearchDatabaseUrl();

      // show "downloading required files" unless we already showed that for
      // highlighting database, in which case show "downloading search data"
      String notificationTitle = getString(R.string.highlighting_database);
      if (haveDownload) {
        notificationTitle = getString(R.string.search_data);
      }

      final String extension = url.endsWith(".zip") ? ".zip" : "";
      Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
          quranFileUtils.getQuranDatabaseDirectory(this), notificationTitle,
          AUDIO_DOWNLOAD_KEY, downloadType);
      intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
          QuranDataProvider.QURAN_ARABIC_DATABASE + extension);
      Timber.d("starting service to download arabic database");
      startService(intent);
    }

    if (downloadType != QuranDownloadService.DOWNLOAD_TYPE_AUDIO) {
      // if audio is playing, just show a status notification
      ToastCompat.makeText(this, R.string.downloading_title,
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (intent == null) {
      return;
    }

    recentPagePresenter.onJump();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      int page = numberOfPages - extras.getInt("page", Constants.PAGES_FIRST);

      boolean currentValue = showingTranslation;
      showingTranslation = extras.getBoolean(EXTRA_JUMP_TO_TRANSLATION, showingTranslation);
      final int highlightedSura = extras.getInt(EXTRA_HIGHLIGHT_SURA, -1);
      final int highlightedAyah = extras.getInt(EXTRA_HIGHLIGHT_AYAH, -1);
      if (highlightedSura > 0 && highlightedAyah > 0) {
        readingEventPresenterBridge.setSelection(highlightedSura, highlightedAyah, true);
      }

      if (showingTranslation != currentValue) {
        if (showingTranslation) {
          pagerAdapter.setTranslationMode();
          updateActionBarSpinner();
        } else {
          pagerAdapter.setQuranMode();
          updateActionBarTitle(numberOfPages - page);
        }

        supportInvalidateOptionsMenu();
      }

      if (highlightedAyah > 0 && highlightedSura > 0) {
        // this will jump to the right page automagically
        ensurePage(highlightedSura, highlightedAyah);
      } else {
        if (isDualPageVisible()) {
          page = page / 2;
        }
        viewPager.setCurrentItem(page);
      }

      setIntent(intent);
    }
  }

  @Override
  public void jumpTo(int page) {
    Intent i = new Intent(this, PagerActivity.class);
    i.putExtra("page", page);
    onNewIntent(i);
  }

  @Override
  public void jumpToAndHighlight(int page, int sura, int ayah) {
    Intent i = new Intent(this, PagerActivity.class);
    i.putExtra("page", page);
    i.putExtra(EXTRA_HIGHLIGHT_SURA, sura);
    i.putExtra(EXTRA_HIGHLIGHT_AYAH, ayah);
    onNewIntent(i);
  }

  @Override
  public void onPause() {
    // Persist last-read page for the home "continue reading" card.
    // onPause fires on every exit (including Back/finish), unlike onSaveInstanceState.
    if (viewPager != null) {
      PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putInt(HOME_LAST_READ_PAGE, getCurrentPage())
          .putInt(HOME_LAST_READ_TOTAL, quranInfo.getNumberOfPages())
          .apply();
    }
    foregroundDisposable.clear();
    if (promptDialog != null) {
      promptDialog.dismiss();
      promptDialog = null;
    }
    audioPresenter.unbind(this);
    recentPagePresenter.unbind(this);
    quranSettings.setWasShowingTranslation(pagerAdapter.isShowingTranslation());
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    Timber.d("onDestroy()");
    clearUiVisibilityListener();

    // remove broadcast receivers
    LocalBroadcastManager.getInstance(this).unregisterReceiver(audioReceiver);
    if (downloadReceiver != null) {
      downloadReceiver.setListener(null);
      LocalBroadcastManager.getInstance(this)
          .unregisterReceiver(downloadReceiver);
      downloadReceiver = null;
    }

    compositeDisposable.dispose();
    audioEventPresenterBridge.dispose();
    readingEventPresenterBridge.dispose();
    handler.removeCallbacksAndMessages(null);
    dismissProgressDialog();
    super.onDestroy();
  }

  private void onSessionEnd() {
    pagerActivityRecitationPresenter.onSessionEnd();
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    int lastPage = quranInfo.getPageFromPosition(viewPager.getCurrentItem(), isDualPageVisible());
    state.putInt(LAST_READ_PAGE, lastPage);
    state.putBoolean(LAST_READING_MODE_IS_TRANSLATION, showingTranslation);
    state.putBoolean(LAST_ACTIONBAR_STATE, isActionBarHidden);
    state.putBoolean(LAST_WAS_DUAL_PAGES, isDualPages);
    state.putBoolean(STATE_AUTO_PLAY_CONSUMED, autoPlayConsumed);
    if (lastAudioRequest != null) {
      state.putParcelable(LAST_AUDIO_REQUEST, lastAudioRequest);
    }
    super.onSaveInstanceState(state);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.quran_menu, menu);
    final MenuItem item = menu.findItem(R.id.search);
    final SearchView searchView = (SearchView) item.getActionView();
    final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    searchView.setQueryHint(getString(R.string.search_hint));
    searchView.setSearchableInfo(searchManager.getSearchableInfo(
        new ComponentName(this, SearchActivity.class)));

    // cache because invalidateOptionsMenu in a toolbar world always calls both
    // onCreateOptionsMenu and onPrepareOptionsMenu, which can be expensive both
    // due to inflation plus due to the search view specific setup work. we can
    // directly modify the bookmark item using a reference to this instead.
    bookmarksMenuItem = menu.findItem(R.id.favorite_item);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = bookmarksMenuItem;
    if (item != null) {
      refreshBookmarksMenu();
    }

    MenuItem quran = menu.findItem(R.id.goto_quran);
    MenuItem translation = menu.findItem(R.id.goto_translation);
    if (quran != null && translation != null) {
      if (!showingTranslation) {
        quran.setVisible(false);
        translation.setVisible(true);
      } else {
        quran.setVisible(true);
        translation.setVisible(false);
      }
    }

    MenuItem nightMode = menu.findItem(R.id.night_mode);
    if (nightMode != null) {
      final boolean isNightMode = quranSettings.isNightMode();
      nightMode.setChecked(isNightMode);
      nightMode.setIcon(isNightMode ? R.drawable.ic_night_mode : R.drawable.ic_day_mode);
    }

    MenuItem focusMode = menu.findItem(R.id.focus_mode);
    if (focusMode != null) {
      focusMode.setChecked(isFocusMode);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.favorite_item) {
      int page = getCurrentPage();
      toggleBookmark(null, null, page);
      return true;
    } else if (itemId == R.id.goto_quran) {
      switchToQuran();
      return true;
    } else if (itemId == R.id.goto_translation) {
      if (translations != null) {
        quranEventLogger.switchToTranslationMode(translations.size());
        switchToTranslation();
      }
      return true;
    } else if (itemId == R.id.night_mode) {
      final boolean isNightMode = !item.isChecked();
      item.setIcon(isNightMode ? R.drawable.ic_night_mode : R.drawable.ic_day_mode);
      item.setChecked(isNightMode);
      // route through the shared reading-mode logic (persists, refreshes pages,
      // clears sepia, and syncs the Day/Sepia/Night chips + nav bar)
      setReadingMode(isNightMode, false);
      return true;
    } else if (itemId == R.id.settings) {
      Intent i = new Intent(this, QuranPreferenceActivity.class);
      startActivity(i);
      return true;
    } else if (itemId == R.id.help) {
      Intent i = new Intent(this, HelpActivity.class);
      startActivity(i);
      return true;
    } else if (itemId == android.R.id.home) {
      onSessionEnd();
      finish();
      return true;
    } else if (itemId == R.id.jump) {
      FragmentManager fm = getSupportFragmentManager();
      JumpFragment jumpDialog = new JumpFragment();
      jumpDialog.show(fm, JumpFragment.TAG);
      return true;
    } else if (itemId == R.id.focus_mode) {
      isFocusMode = !isFocusMode;
      item.setChecked(isFocusMode);
      if (isFocusMode) {
        // Hide action bar and audio bar, enter immersive
        toggleActionBarVisibility(false);
        setUiVisibilityKitKat(false);
        android.widget.Toast.makeText(this, R.string.focus_mode_on, android.widget.Toast.LENGTH_SHORT).show();
      } else {
        // Restore normal mode
        setUiVisibilityKitKat(true);
        toggleActionBarVisibility(true);
        android.widget.Toast.makeText(this, R.string.focus_mode_off, android.widget.Toast.LENGTH_SHORT).show();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void changeTranslationFontSize(int delta) {
    quranSettings.setTranslationTextSize(quranSettings.getTranslationTextSize() + delta);
    refreshQuranPages();
  }

  private void updateTranslationSourceName() {
    if (translationSourceName == null) {
      return;
    }
    String name = getString(R.string.menu_translation);
    if (translationNames != null && translationNames.length > 0) {
      name = translationNames[0];
      if (translationNames.length > 1) {
        name = name + "  +" + (translationNames.length - 1);
      }
    }
    translationSourceName.setText(name);
  }

  /**
   * Single source of truth for the text-mode font/source strip. Shows it whenever we're
   * in translation mode (including when the reader is restored/opened directly into text
   * mode, where switchToTranslation() is never called) and hides it otherwise.
   */
  private void updateTranslationSourceStrip() {
    if (translationSourceStrip == null) {
      return;
    }
    if (showingTranslation) {
      translationSourceStrip.setVisibility(View.VISIBLE);
      translationSourceStrip.setTranslationY(isActionBarHidden
          ? -(toolBarArea.getHeight() + translationSourceStrip.getHeight()) : 0);
      updateTranslationSourceName();
    } else {
      translationSourceStrip.setVisibility(View.GONE);
    }
  }

  private void refreshQuranPages() {
    int pos = viewPager.getCurrentItem();
    int start = (pos == 0) ? pos : pos - 1;
    int end = (pos == pagerAdapter.getCount() - 1) ? pos : pos + 1;
    for (int i = start; i <= end; i++) {
      Fragment f = pagerAdapter.getFragmentIfExists(i);
      if (f instanceof QuranPage) {
        ((QuranPage) f).updateView();
      }
    }
  }

  @Override
  public boolean onSearchRequested() {
    return super.onSearchRequested();
  }

  private void switchToQuran() {
    if (getSelectionStart() != null) {
      endAyahMode();
    }
    final int page = getCurrentPage();
    pagerAdapter.setQuranMode();
    showingTranslation = false;
    if (shouldUpdatePageNumber()) {
      final int position = quranInfo.getPositionFromPage(page, true);
      viewPager.setCurrentItem(position);
    }
    updateTranslationSourceStrip();

    supportInvalidateOptionsMenu();
    updateActionBarTitle(page);
  }

  private void switchToTranslation() {
    if (getSelectionStart() != null) {
      endAyahMode();
    }

    if (translations.size() == 0) {
      startTranslationManager();
    } else {
      int page = getCurrentPage();
      pagerAdapter.setTranslationMode();
      showingTranslation = true;
      if (shouldUpdatePageNumber()) {
        if (page % 2 == 0) {
          page--;
        }
        final int position = quranInfo.getPositionFromPage(page, false);
        viewPager.setCurrentItem(position);
      }
      updateTranslationSourceStrip();
      supportInvalidateOptionsMenu();
      updateActionBarSpinner();
    }

    if (!quranFileUtils.hasArabicSearchDatabase() && !promptedForExtraDownload) {
      promptedForExtraDownload = true;
      showGetRequiredFilesDialog();
    }
  }

  public void startTranslationManager() {
    startActivity(new Intent(this, TranslationManagerActivity.class));
  }

  private final TranslationsSpinnerAdapter.OnSelectionChangedListener translationItemChangedListener =
      selectedItems -> {
        quranSettings.setActiveTranslations(selectedItems);
        int pos = viewPager.getCurrentItem() - 1;
        for (int count = 0; count < 3; count++) {
          if (pos + count < 0) {
            continue;
          }
          Fragment f = pagerAdapter.getFragmentIfExists(pos + count);
          if (f instanceof TranslationFragment) {
            ((TranslationFragment) f).refresh();
          } else if (f instanceof TabletFragment) {
            ((TabletFragment) f).refresh();
          }
        }
      };

  public List<LocalTranslation> getTranslations() {
    return translations;
  }

  public String[] getTranslationNames() {
    return translationNames;
  }

  public Set<String> getActiveTranslationsFilesNames() {
    return activeTranslationsFilesNames;
  }

  @Override
  public void onAddTagSelected() {
    FragmentManager fm = getSupportFragmentManager();
    AddTagDialog dialog = new AddTagDialog();
    dialog.show(fm, AddTagDialog.TAG);
  }

  private void updateActionBarTitle(int page) {
    String sura = quranDisplayData.getSuraNameFromPage(this, page, true);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      translationsSpinner.setVisibility(View.GONE);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setTitle(sura);
      String desc = quranDisplayData.getPageSubtitle(this, page);
      actionBar.setSubtitle(desc);
      // Amiri's tall diacritics + font padding clipped the subtitle inside the
      // toolbar; drop the extra padding so surah name + "Page N · Juz M" both show.
      stripToolbarTitlePadding();
    }
  }

  private void stripToolbarTitlePadding() {
    if (toolbar == null) {
      return;
    }
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      View child = toolbar.getChildAt(i);
      if (child instanceof TextView) {
        ((TextView) child).setIncludeFontPadding(false);
      }
    }
  }

  private void refreshActionBarSpinner() {
    if (translationsSpinnerAdapter != null) {
      translationsSpinnerAdapter.notifyDataSetChanged();
    } else {
      updateActionBarSpinner();
    }
  }

  private int getCurrentPage() {
    return quranInfo.getPageFromPosition(viewPager.getCurrentItem(), isDualPageVisible());
  }

  private void updateActionBarSpinner() {
    if (translationNames == null || translationNames.length == 0) {
      int page = getCurrentPage();
      updateActionBarTitle(page);
      return;
    }

    if (translationsSpinnerAdapter == null) {
      translationsSpinnerAdapter = new TranslationsSpinnerAdapter(this,
          R.layout.translation_ab_spinner_item, translationNames, translations,
          activeTranslationsFilesNames == null ? quranSettings.getActiveTranslations() : activeTranslationsFilesNames,
          translationItemChangedListener) {
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
          int type = super.getItemViewType(position);
          convertView = super.getView(position, convertView, parent);
          if (type == 0) {
            SpinnerHolder holder = (SpinnerHolder) convertView.getTag();
            int page = getCurrentPage();

            String sura = quranDisplayData.getSuraNameFromPage(PagerActivity.this, page, true);
            holder.title.setText(sura);
            String desc = quranDisplayData.getPageSubtitle(PagerActivity.this, page);
            holder.subtitle.setText(desc);
            holder.subtitle.setVisibility(View.VISIBLE);
          }
          return convertView;
        }
      };
      translationsSpinner.setAdapter(translationsSpinnerAdapter);
    }

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
      translationsSpinner.setVisibility(View.VISIBLE);
    }
  }

  private final BroadcastReceiver audioReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null) {
        int state = intent.getIntExtra(
            AudioService.AudioUpdateIntent.STATUS, -1);
        int repeatCount = intent.getIntExtra(
            AudioService.AudioUpdateIntent.REPEAT_COUNT, -200);
        AudioRequest request = IntentCompat.getParcelableExtra(intent, AudioService.AudioUpdateIntent.REQUEST, AudioRequest.class);
        if (request != null) {
          lastAudioRequest = request;
        }
        if (state == AudioService.AudioUpdateIntent.PLAYING) {
          audioStatusBar.switchMode(AudioStatusBar.PLAYING_MODE);
          if (repeatCount >= -1) {
            audioStatusBar.setRepeatCount(repeatCount);
          }
        } else if (state == AudioService.AudioUpdateIntent.PAUSED) {
          audioStatusBar.switchMode(AudioStatusBar.PAUSED_MODE);
        } else if (state == AudioService.AudioUpdateIntent.STOPPED) {
          audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
          audioStatusBar.setNowPlayingInfo("");
          lastAudioRequest = null;
        }
      }
    }
  };

  @Override
  public void updateDownloadProgress(int progress,
                                     long downloadedSize, long totalSize) {
    audioStatusBar.switchMode(
        AudioStatusBar.DOWNLOADING_MODE);
    audioStatusBar.setProgress(progress);
  }

  @Override
  public void updateProcessingProgress(int progress,
                                       int processFiles, int totalFiles) {
    audioStatusBar.setProgressText(getString(R.string.extracting_title), false);
    audioStatusBar.setProgress(-1);
  }

  @Override
  public void handleDownloadTemporaryError(int errorId) {
    audioStatusBar.setProgressText(getString(errorId), false);
  }

  @Override
  public void handleDownloadSuccess() {
    refreshQuranPages();
    audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
    audioPresenter.onDownloadSuccess();
  }

  @Override
  public void handleDownloadFailure(int errId) {
    String s = getString(errId);
    audioStatusBar.setProgressText(s, true);
  }

  public void toggleActionBarVisibility(boolean visible) {
    if (visible == isActionBarHidden) {
      toggleActionBar();
    }
  }

  public void toggleActionBar() {
    if (isActionBarHidden) {
      setUiVisibility(true);

      isActionBarHidden = false;
    } else {
      handler.removeMessages(MSG_HIDE_ACTIONBAR);
      setUiVisibility(false);

      isActionBarHidden = true;
    }
  }

  private void ensurePage(int sura, int ayah) {
    int page = quranInfo.getPageFromSuraAyah(sura, ayah);
    if (page >= Constants.PAGES_FIRST && page <= numberOfPages) {
      int position = quranInfo.getPositionFromPage(page, isDualPageVisible());
      if (position != viewPager.getCurrentItem()) {
        viewPager.setCurrentItem(position);
      }
    }
  }

  private void requestTranslationsList() {
    compositeDisposable.add(
        Single.fromCallable(() ->
            translationsDBAdapter.getTranslations())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<List<LocalTranslation>>() {
              @Override
              public void onSuccess(@NonNull List<LocalTranslation> translationList) {
                final List<LocalTranslation> sortedTranslations = new ArrayList<>(translationList);
              Collections.sort(sortedTranslations, new LocalTranslationDisplaySort());

              int items = sortedTranslations.size();
                String[] titles = new String[items];
                for (int i = 0; i < items; i++) {
                  LocalTranslation item = sortedTranslations.get(i);
                  if (!TextUtils.isEmpty(item.getTranslatorForeign())) {
                    titles[i] = item.getTranslatorForeign();
                  } else if (!TextUtils.isEmpty(item.getTranslator())) {
                    titles[i] = item.getTranslator();
                  } else {
                    titles[i] = item.getName();
                  }
                }

                Set<String> currentActiveTranslationsFilesNames = quranSettings.getActiveTranslations();
                if (currentActiveTranslationsFilesNames.isEmpty() && items > 0) {
                  currentActiveTranslationsFilesNames = new HashSet<>();
                  for (int i = 0; i < items; i++) {
                    currentActiveTranslationsFilesNames.add(sortedTranslations.get(i).getFilename());
                  }
                }
                activeTranslationsFilesNames = currentActiveTranslationsFilesNames;

                if (translationsSpinnerAdapter != null) {
                  translationsSpinnerAdapter
                      .updateItems(titles, sortedTranslations, activeTranslationsFilesNames);
                }
                translationNames = titles;
                translations = sortedTranslations;

                if (showingTranslation) {
                  // Since translation items have changed, need to
                  updateActionBarSpinner();
                  // names are only known now, so (re)show + label the source strip —
                  // covers the reader being opened directly into text mode
                  updateTranslationSourceStrip();
                }
              }

              @Override
              public void onError(@NonNull Throwable e) {
              }
            }));
  }

  private void toggleBookmark(final Integer sura, final Integer ayah, final int page) {
    compositeDisposable.add(bookmarkModel.toggleBookmarkObservable(sura, ayah, page)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override
          public void onSuccess(@NonNull Boolean isBookmarked) {
            if (sura == null || ayah == null) {
              // page bookmark
              bookmarksCache.put(page, isBookmarked);
              bookmarksMenuItem.setIcon(isBookmarked ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
            } else {
              // ayah bookmark
              SuraAyah suraAyah = new SuraAyah(sura, ayah);
              updateAyahBookmark(suraAyah, isBookmarked);
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {
          }
        }));
  }

  private void checkIfPageIsBookmarked(Integer... pages) {
    compositeDisposable.add(bookmarkModel.getIsBookmarkedObservable(pages)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<Pair<Integer, Boolean>>() {
          @Override
          public void onNext(@NonNull Pair<Integer, Boolean> result) {
            bookmarksCache.put(result.first, result.second);
          }

          @Override
          public void onError(Throwable e) {
          }

          @Override
          public void onComplete() {
            refreshBookmarksMenu();
          }
        }));
  }

  private void refreshBookmarksMenu() {
    final MenuItem menuItem = bookmarksMenuItem;
    if (menuItem != null) {
      int page = quranInfo.getPageFromPosition(viewPager.getCurrentItem(), isDualPageVisible());

      boolean bookmarked = false;
      if (bookmarksCache.indexOfKey(page) >= 0) {
        bookmarked = bookmarksCache.get(page);
      }

      if (!bookmarked && isDualPageVisible() &&
          bookmarksCache.indexOfKey(page - 1) >= 0) {
        bookmarked = bookmarksCache.get(page - 1);
      }

      menuItem.setIcon(bookmarked ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
    } else {
      supportInvalidateOptionsMenu();
    }
  }

  // region Audio playback

  @Override
  public void onPlayPressed() {
    if (audioStatusBar.getCurrentMode() == AudioStatusBar.PAUSED_MODE) {
      // if we are "paused," just un-pause.
      forceStreamOnce = false; // never carry a stale one-shot into an un-pause
      handlePlayback(null);
      return;
    }

    int position = viewPager.getCurrentItem();
    int page = numberOfPages - position;
    if (isDualPageVisible()) {
      // first page of the spread; getPageFromPosition clamps odd-count sets
      page = quranInfo.getPageFromPosition(position, true) - 1;
    }

    // log the event
    quranEventLogger.logAudioPlayback(QuranEventLogger.AudioPlaybackSource.PAGE,
        audioStatusBar.getAudioInfo(), isDualPages, showingTranslation, isSplitScreen);

    int startSura = quranDisplayData.safelyGetSuraOnPage(page);
    int startAyah = quranInfo.getFirstAyahOnPage(page);
    List<Integer> startingSuraList = quranInfo.getListOfSurahWithStartingOnPage(page);
    // a drill remembered for this page always gets the chooser, so pressing
    // play doesn't silently fall back to page defaults and lose the repeats
    final boolean offerLastRange = lastPlaybackRangeIntersectsPage(page);
    if (!offerLastRange &&
        (startingSuraList.size() == 0 ||
        (startingSuraList.size() == 1 && startingSuraList.get(0) == startSura))) {
      playFromAyah(page, startSura, startAyah);
    } else {
      // The multi-sura chooser is interactive; don't carry the one-shot stream flag
      // into the deferred (or dismissed) selection.
      forceStreamOnce = false;
      promptForMultipleChoicePlay(page, startSura, startAyah, startingSuraList);
    }
  }

  private void playFromAyah(int page, int startSura, int startAyah) {
    final SuraAyah start = new SuraAyah(startSura, startAyah);
    final SuraAyah end = getSelectionEnd();
    // handle the case of multiple ayat being selected and play them as a range if so
    final SuraAyah ending = (end == null || start.equals(end) || start.after(end))? null : end;
    playFromAyah(start, ending, page, 0, 0, ending != null);
  }

  public void playFromAyah(SuraAyah start,
                           SuraAyah end,
                           int page,
                           int verseRepeat,
                           int rangeRepeat,
                           boolean enforceRange) {
    // Consume the one-shot stream flag up front so it can never leak into a later
    // play (e.g. an early-return below). forceStream lets the Home read+listen
    // auto-start stream without a download dialog.
    final boolean forceStream = forceStreamOnce;
    forceStreamOnce = false;

    final SuraAyah ending = end != null ? end :
        audioUtils.getLastAyahToPlay(start, page,
            quranSettings.getPreferredDownloadAmount(), isDualPageVisible());

    if (ending != null) {
      Timber.d("playFromAyah - " + start + ", ending: " +
          ending + " - original: " + end + " -- " +
          quranSettings.getPreferredDownloadAmount());
      final QariItem item = audioStatusBar.getAudioInfo();
      final boolean shouldStream = forceStream || quranSettings.shouldStream();
      audioPresenter.play(
          start, ending, item, verseRepeat, rangeRepeat, enforceRange, shouldStream);
    }
  }

  public void handleRequiredDownload(Intent downloadIntent) {
    boolean needsPermission = needsPermissionToDownloadOver3g;
    if (needsPermission) {
      if (QuranUtils.isOnWifiNetwork(this)) {
        Timber.d("on wifi, don't need permission for download...");
        needsPermission = false;
      }
    }

    if (needsPermission) {
      audioStatusBar.switchMode(AudioStatusBar.PROMPT_DOWNLOAD_MODE);
    } else {
      if (isActionBarHidden) {
        toggleActionBar();
      }
      audioStatusBar.switchMode(AudioStatusBar.DOWNLOADING_MODE);
      Timber.d("starting service in handleRequiredDownload");
      startService(downloadIntent);
    }
  }

  public void handlePlayback(AudioRequest request) {
    needsPermissionToDownloadOver3g = true;
    final Intent intent = new Intent(this, AudioService.class);
    intent.setAction(AudioService.ACTION_PLAYBACK);
    if (request != null) {
      intent.putExtra(AudioService.EXTRA_PLAY_INFO, request);
      lastAudioRequest = request;
      audioStatusBar.setRepeatCount(request.getRepeatInfo());
      audioStatusBar.switchMode(AudioStatusBar.LOADING_MODE);
    }

    Timber.d("starting service for audio playback");
    startService(intent);
  }

  @Override
  public void onPausePressed() {
    startService(audioUtils.getAudioIntent(
        this, AudioService.ACTION_PAUSE));
    audioStatusBar.switchMode(AudioStatusBar.PAUSED_MODE);
  }

  @Override
  public void onNextPressed() {
    startService(audioUtils.getAudioIntent(this,
        AudioService.ACTION_SKIP));
  }

  @Override
  public void onPreviousPressed() {
    startService(audioUtils.getAudioIntent(this,
        AudioService.ACTION_REWIND));
  }

  @Override
  public void onAudioSettingsPressed() {
    showSlider(slidingPagerAdapter.getPagePosition(AUDIO_PAGE));
  }

  public boolean updatePlayOptions(int rangeRepeat,
                                   int verseRepeat, boolean enforceRange) {
    if (lastAudioRequest != null) {
      final AudioRequest updatedAudioRequest = new AudioRequest(lastAudioRequest.getStart(),
          lastAudioRequest.getEnd(),
          lastAudioRequest.getQari(),
          verseRepeat,
          rangeRepeat,
          enforceRange,
          lastAudioRequest.getShouldStream(),
          lastAudioRequest.getAudioPathInfo());
      Intent i = new Intent(this, AudioService.class);
      i.setAction(AudioService.ACTION_UPDATE_REPEAT);
      i.putExtra(AudioService.EXTRA_PLAY_INFO, updatedAudioRequest);
      startService(i);

      lastAudioRequest = updatedAudioRequest;
      audioStatusBar.setRepeatCount(verseRepeat);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setRepeatCount(int repeatCount) {
    if (lastAudioRequest != null) {
      final AudioRequest updatedAudioRequest = new AudioRequest(lastAudioRequest.getStart(),
          lastAudioRequest.getEnd(),
          lastAudioRequest.getQari(),
          repeatCount,
          lastAudioRequest.getRangeRepeatInfo(),
          lastAudioRequest.getEnforceBounds(),
          lastAudioRequest.getShouldStream(),
          lastAudioRequest.getAudioPathInfo());

      Intent i = new Intent(this, AudioService.class);
      i.setAction(AudioService.ACTION_UPDATE_REPEAT);
      i.putExtra(AudioService.EXTRA_PLAY_INFO, updatedAudioRequest);
      startService(i);
      lastAudioRequest = updatedAudioRequest;
    }
  }

  @Override
  public void onStopPressed() {
    startService(audioUtils.getAudioIntent(this, AudioService.ACTION_STOP));
    audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
    lastAudioRequest = null;
  }

  @Override
  public void onCancelPressed(boolean cancelDownload) {
    if (cancelDownload) {
      needsPermissionToDownloadOver3g = true;

      int resId = R.string.canceling;
      audioStatusBar.setProgressText(getString(resId), true);
      Intent i = new Intent(this, QuranDownloadService.class);
      i.setAction(QuranDownloadService.ACTION_CANCEL_DOWNLOADS);
      startService(i);
    } else {
      audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
      startService(audioUtils.getAudioIntent(this, AudioService.ACTION_STOP));
    }
  }

  @Override
  public void onAcceptPressed() {
    needsPermissionToDownloadOver3g = false;
    audioPresenter.onDownloadPermissionGranted();
  }

  //endregion

  @Override
  public void onBackPressed() {
    if (getSelectionStart() != null) {
      endAyahMode();
    } else if (showingTranslation) {
      switchToQuran();
    } else {
      onSessionEnd();
      super.onBackPressed();
    }
  }

  // region Ayah selection

  private SuraAyah getSelectionStart() {
    final AyahSelection currentSelection = readingEventPresenter.currentAyahSelection();
    return AyahSelectionKt.startSuraAyah(currentSelection);
  }

  private SuraAyah getSelectionEnd() {
    final AyahSelection currentSelection = readingEventPresenter.currentAyahSelection();
    return AyahSelectionKt.endSuraAyah(currentSelection);
  }

  public AudioRequest getLastAudioRequest() {
    return lastAudioRequest;
  }

  public void endAyahMode() {
    readingEventPresenterBridge.clearSelectedAyah();
    slidingPanel.collapsePane();
  }

  //endregion

  private void updateLocalTranslations(final SuraAyah start) {
    final AyahTracker ayahTracker = resolveCurrentTracker();
    if (ayahTracker != null) {
      lastActivatedLocalTranslations = ayahTracker.getLocalTranslations();
      lastSelectedTranslationAyah = ayahTracker.getQuranAyahInfo(start.sura, start.ayah);
    }
  }

  private AyahTracker resolveCurrentTracker() {
    int position = viewPager.getCurrentItem();
    Fragment f = pagerAdapter.getFragmentIfExists(position);
    if (f instanceof QuranPage && f.isVisible()) {
      return ((QuranPage) f).getAyahTracker();
    } else {
      return null;
    }
  }

  private class AyahMenuItemSelectionHandler implements MenuItem.OnMenuItemClickListener {
    @Override
    public boolean onMenuItemClick(MenuItem item) {
      int sliderPage = -1;
      final AyahSelection currentSelection = readingEventPresenter.currentAyahSelection();
      final SuraAyah startSuraAyah = AyahSelectionKt.startSuraAyah(currentSelection);
      final SuraAyah endSuraAyah = AyahSelectionKt.endSuraAyah(currentSelection);
      if (startSuraAyah == null || endSuraAyah == null) {
        return false;
      }

      switch (item.getItemId()) {
        case R.id.cab_bookmark_ayah:
          final int startPage = quranInfo.getPageFromSuraAyah(startSuraAyah.sura, startSuraAyah.ayah);
          toggleBookmark(startSuraAyah.sura, startSuraAyah.ayah, startPage);
          break;
        case R.id.cab_tag_ayah:
          sliderPage = slidingPagerAdapter.getPagePosition(TAG_PAGE);
          break;
        case R.id.cab_translate_ayah:
          sliderPage = slidingPagerAdapter.getPagePosition(TRANSLATION_PAGE);
          break;
        case R.id.cab_play_from_here:
          quranEventLogger.logAudioPlayback(QuranEventLogger.AudioPlaybackSource.AYAH,
              audioStatusBar.getAudioInfo(), isDualPages, showingTranslation, isSplitScreen);
          playFromAyah(getCurrentPage(), startSuraAyah.sura, startSuraAyah.ayah);
          toggleActionBarVisibility(true);
          sliderPage = -1;
          break;
        case R.id.cab_recite_from_here:
          pagerActivityRecitationPresenter.onRecitationPressed();
          sliderPage = -1;
          break;
        case R.id.cab_share_ayah_link:
          shareAyahLink(startSuraAyah, endSuraAyah);
          break;
        case R.id.cab_share_ayah_text:
          shareAyah(startSuraAyah, endSuraAyah, false);
          break;
        case R.id.cab_copy_ayah:
          shareAyah(startSuraAyah, endSuraAyah, true);
          break;
        default:
          return false;
      }
      if (sliderPage < 0) {
        endAyahMode();
      } else {
        showSlider(sliderPage);
      }
      return true;
    }
  }

  private void shareAyah(SuraAyah start, SuraAyah end, final boolean isCopy) {
    if (start == null || end == null) {
      return;
    } else if (!quranFileUtils.hasArabicSearchDatabase()) {
      showGetRequiredFilesDialog();
      return;
    }

    final LocalTranslation[] translationNames = lastActivatedLocalTranslations;
    if (showingTranslation && translationNames != null) {

      // temporarily required so "lastSelectedTranslationAyah" isn't null
      // the real solution is to move this sharing logic out of PagerActivity
      // in the future and avoid this back and forth with the translation fragment.
      updateLocalTranslations(start);
      final QuranAyahInfo quranAyahInfo = lastSelectedTranslationAyah;
      if (quranAyahInfo != null) {
        final String shareText = shareUtil.getShareText(this, quranAyahInfo, translationNames);
        if (isCopy) {
          shareUtil.copyToClipboard(this, shareText);
        } else {
          shareUtil.shareViaIntent(this, shareText, R.string.share_ayah_text);
        }
      }

      return;
    }

    compositeDisposable.add(
        arabicDatabaseUtils
            .getVerses(start, end)
            .filter(quranAyahs -> quranAyahs.size() > 0)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(quranAyahs -> {
              if (isCopy) {
                shareUtil.copyVerses(PagerActivity.this, quranAyahs);
              } else {
                shareUtil.shareVerses(PagerActivity.this, quranAyahs);
              }
            }));
  }

  public void shareAyahLink(SuraAyah start, SuraAyah end) {
    showProgressDialog();
    compositeDisposable.add(
        quranAppUtils.getQuranAppUrlObservable(getString(R.string.quranapp_key), start, end)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<String>() {
              @Override
              public void onSuccess(@NonNull String url) {
                shareUtil.shareViaIntent(PagerActivity.this, url, R.string.share_ayah);
                dismissProgressDialog();
              }

              @Override
              public void onError(@NonNull Throwable e) {
                dismissProgressDialog();
              }
            })
    );
  }

  private void showProgressDialog() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(this);
      progressDialog.setIndeterminate(true);
      progressDialog.setMessage(getString(R.string.index_loading));
      progressDialog.show();
    }
  }

  private void dismissProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
    progressDialog = null;
  }

  private void showSlider(int sliderPage) {
    readingEventPresenterBridge.clearMenuForSelection();
    slidingPager.setCurrentItem(sliderPage);
    slidingPanel.showPane();
    // TODO there's got to be a better way than this hack
    // The issue is that smoothScrollTo returns if mCanSlide is false
    // and it's false when the panel is GONE and showPane only calls
    // requestLayout, and only in onLayout does mCanSlide become true.
    // So by posting this later it gives time for onLayout to run.
    handler.post(() -> slidingPanel.expandPane());
  }

  private void updateAyahBookmark(SuraAyah suraAyah, boolean bookmarked) {
    // Refresh toolbar icon
    final SuraAyah start = getSelectionStart();
    if (start != null && start.equals(suraAyah)) {
      ayahToolBar.setBookmarked(bookmarked);
    }
  }

  /** The page range of the drill remembered by the playback panel, if any. */
  private boolean lastPlaybackRangeIntersectsPage(int page) {
    if (!quranSettings.hasLastPlaybackRange()) {
      return false;
    }
    final int startPage = quranInfo.getPageFromSuraAyah(
        quranSettings.getLastPlaybackStartSura(), quranSettings.getLastPlaybackStartAyah());
    final int endPage = quranInfo.getPageFromSuraAyah(
        quranSettings.getLastPlaybackEndSura(), quranSettings.getLastPlaybackEndAyah());
    return page >= startPage && page <= endPage;
  }

  private String lastPlaybackRangeSummary(SuraAyah start, SuraAyah end) {
    final String startText = quranDisplayData.getSuraName(this, start.sura, false) +
        ' ' + QuranUtils.getLocalizedNumber(this, start.ayah);
    final String range = start.sura == end.sura
        ? startText + '–' + QuranUtils.getLocalizedNumber(this, end.ayah)
        : startText + " – " + quranDisplayData.getSuraName(this, end.sura, false) +
            ' ' + QuranUtils.getLocalizedNumber(this, end.ayah);
    final boolean hasRepeats = quranSettings.getLastPlaybackVerseRepeat() != 0
        || quranSettings.getLastPlaybackRangeRepeat() != 0;
    return hasRepeats
        ? range + " · " + getString(R.string.playback_prompt_repeat_suffix)
        : range;
  }

  private void promptForMultipleChoicePlay(int page, int startSura, int startAyah,
                                           List<Integer> startingSuraList) {
    // navy playback sheet (mockup playback-sheet-redesign): an optional
    // "repeat last range" row restores the remembered drill, sura rows carry
    // a "from the start of the surah" sublabel, and an optional page-start
    // row leads with the page number
    final List<AppBottomSheet.PlaybackStartOption> options = new ArrayList<>();
    final List<Runnable> actions = new ArrayList<>();

    if (lastPlaybackRangeIntersectsPage(page)) {
      final SuraAyah rangeStart = new SuraAyah(
          quranSettings.getLastPlaybackStartSura(), quranSettings.getLastPlaybackStartAyah());
      final SuraAyah rangeEnd = new SuraAyah(
          quranSettings.getLastPlaybackEndSura(), quranSettings.getLastPlaybackEndAyah());
      final int verseRepeat = quranSettings.getLastPlaybackVerseRepeat();
      final int rangeRepeat = quranSettings.getLastPlaybackRangeRepeat();
      final boolean enforce = quranSettings.getLastPlaybackEnforceBounds();
      options.add(new AppBottomSheet.PlaybackStartOption(
          R.drawable.ic_repeat,
          getString(R.string.playback_prompt_last_range),
          lastPlaybackRangeSummary(rangeStart, rangeEnd)));
      actions.add(() -> playFromAyah(rangeStart, rangeEnd,
          quranInfo.getPageFromSuraAyah(rangeStart.sura, rangeStart.ayah),
          verseRepeat, rangeRepeat, enforce));
    }

    if (startingSuraList.isEmpty() || startSura != startingSuraList.get(0)) {
      options.add(new AppBottomSheet.PlaybackStartOption(
          R.drawable.ic_play,
          getString(R.string.starting_page_label),
          getString(R.string.playback_prompt_page_sub,
              QuranUtils.getLocalizedNumber(this, page))));
      actions.add(() -> playFromAyah(page, startSura, startAyah));
    }

    for (Integer sura : startingSuraList) {
      options.add(new AppBottomSheet.PlaybackStartOption(
          R.drawable.ic_transcript,
          quranDisplayData.getSuraName(this, sura, true),
          getString(R.string.playback_prompt_sura_sub)));
      actions.add(() -> playFromAyah(page, sura, 1));
    }

    promptDialog = AppBottomSheet.showPlaybackStartOptions(this,
        getString(R.string.playback_prompt_reciter, audioStatusBar.getAudioInfo().getName()),
        options,
        i -> {
          actions.get(i).run();
          promptDialog = null;
        });
  }

  private class SlidingPanelListener implements SlidingUpPanelLayout.PanelSlideListener {

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelCollapsed(View panel) {
      if (getSelectionStart() != null) {
        endAyahMode();
      }
      slidingPanel.hidePane();
      readingEventPresenter.onPanelClosed();
    }

    @Override
    public void onPanelExpanded(View panel) {
      readingEventPresenter.onPanelOpened();
    }

    @Override
    public void onPanelAnchored(View panel) {
    }
  }
}
