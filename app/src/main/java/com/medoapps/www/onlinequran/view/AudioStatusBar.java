package com.medoapps.www.onlinequran.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.common.audio.QariItem;
import com.medoapps.www.onlinequran.data.Constants;
import com.medoapps.www.onlinequran.util.QuranSettings;
import com.medoapps.www.onlinequran.util.QuranUtils;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;

public class AudioStatusBar extends LeftToRightLinearLayout {

  public static final int STOPPED_MODE = 1;
  public static final int DOWNLOADING_MODE = 2;
  public static final int LOADING_MODE = 3;
  public static final int PLAYING_MODE = 4;
  public static final int PAUSED_MODE = 5;
  public static final int PROMPT_DOWNLOAD_MODE = 6;
  public static final int RECITATION_LISTENING_MODE = 7;
  public static final int RECITATION_STOPPED_MODE = 8;
  public static final int RECITATION_PLAYING_MODE = 9;

  private static final int MAX_AUDIOBAR_QUICK_REPEAT = 3;

  private final Context context;
  private int currentMode;
  private final int buttonWidth;
  private final int separatorWidth;
  private final int separatorSpacing;
  private final int textFontSize;
  private final int textFullFontSize;
  private final int spinnerPadding;
  // chosen footer design (mockup 18): vertical root stacking one row (stopped/
  // download/prompt) or reciter-deck + controls-row (playing)
  private final int rowHeight;
  private final int deckHeight;
  private final int fabSize;
  private final int avatarSize;
  private LinearLayout currentRow;
  private QariAdapter adapter;
  private QariAdapter deckAdapter;
  private TextView nowPlayingText;
  private CharSequence nowPlayingInfo = "";

  private int currentQari;
  private int currentRepeat = 0;
  @DrawableRes private int itemBackground;
  private final boolean isRtl;
  private boolean isDualPageMode;
  private boolean isRecitationEnabled;
  private boolean hasErrorText;
  private boolean haveCriticalError = false;
  private final SharedPreferences sharedPreferences;

  private QuranSpinner spinner;
  private TextView progressText;
  private ProgressBar progressBar;
  private final RepeatButton repeatButton;
  private AudioBarListener audioBarListener;
  private AudioBarRecitationListener audioBarRecitationListener;

  public interface AudioBarListener {
    void onPlayPressed();
    void onPausePressed();
    void onNextPressed();
    void onPreviousPressed();
    void onStopPressed();
    void onCancelPressed(boolean stopDownload);
    void setRepeatCount(int repeatCount);
    void onAcceptPressed();
    void onAudioSettingsPressed();
  }

  public interface AudioBarRecitationListener {
    void onRecitationPressed();
    void onRecitationLongPressed();
    void onRecitationTranscriptPressed();
    void onHideVersesPressed();
    void onEndRecitationSessionPressed();
    void onPlayRecitationPressed();
    void onPauseRecitationPressed();
  }

  public AudioStatusBar(Context context) {
    this(context, null);
  }

  public AudioStatusBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AudioStatusBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    this.context = context;
    repeatButton = new RepeatButton(context);
    Resources resources = getResources();
    buttonWidth = resources.getDimensionPixelSize(
        R.dimen.audiobar_button_width);
    separatorWidth = resources.getDimensionPixelSize(
        R.dimen.audiobar_separator_width);
    separatorSpacing = resources.getDimensionPixelSize(
        R.dimen.audiobar_separator_padding);
    textFontSize = resources.getDimensionPixelSize(
        R.dimen.audiobar_text_font_size);
    textFullFontSize = resources.getDimensionPixelSize(
        R.dimen.audiobar_text_full_font_size);
    spinnerPadding = resources
        .getDimensionPixelSize(R.dimen.audiobar_spinner_padding);
    rowHeight = resources.getDimensionPixelSize(R.dimen.audiobar_height);
    deckHeight = resources.getDimensionPixelSize(R.dimen.audiobar_deck_height);
    fabSize = Math.min(resources.getDimensionPixelSize(R.dimen.audiobar_fab_size),
        rowHeight - separatorSpacing);
    avatarSize = Math.min(resources.getDimensionPixelSize(R.dimen.audiobar_avatar_size),
        rowHeight - separatorSpacing);
    // rows stack vertically; each mode adds its own horizontal row(s)
    setOrientation(LinearLayout.VERTICAL);

    // only flip the layout when the language is rtl and we're on api 17+
    isRtl = QuranSettings.getInstance(this.context).isArabicNames() || QuranUtils.isRtl();
    sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(context.getApplicationContext());
    currentQari = sharedPreferences.getInt(Constants.PREF_DEFAULT_QARI, 0);

    itemBackground = 0;
    if (attrs != null) {
      TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AudioStatusBar);
      itemBackground = ta.getResourceId(R.styleable.AudioStatusBar_android_itemBackground,
          itemBackground);
      ta.recycle();
    }
  }

  public void setIsDualPageMode(boolean isDualPageMode) {
    this.isDualPageMode = isDualPageMode;
  }

  public boolean getIsRecitationEnabled() {
    return isRecitationEnabled;
  }

  public void setIsRecitationEnabled(boolean isEnabled) {
    this.isRecitationEnabled = isEnabled;
  }

  public void setQariList(List<QariItem> qariList) {
    // TODO: optimize - PREF_DEFAULT_QARI is the qari id, should introduce a helper pref for pos
    final int qaris = qariList.size();
    if (currentQari >= qaris || qariList.get(currentQari).getId() != currentQari) {
      // figure out the updated position for the index
      int updatedIndex = 0;
      for (int i = 0; i < qaris; i++) {
        if (qariList.get(i).getId() == currentQari) {
          updatedIndex = i;
          break;
        }
      }
      currentQari = updatedIndex;
    }

    // Two collapsed looks over the same navy dropdown rows (mockup 18/06b):
    // full = name + status subline (stopped), deck = compact name (playing).
    adapter = new QariAdapter(this.context, qariList,
        R.layout.audiobar_spinner_item_full, R.layout.audio_panel_spinner_dropdown_item);
    deckAdapter = new QariAdapter(this.context, qariList,
        R.layout.audiobar_spinner_item, R.layout.audio_panel_spinner_dropdown_item);
    showStoppedMode();
  }

  public int getCurrentMode() {
    return currentMode;
  }

  public void switchMode(int mode) {
    switchMode(mode, false);
  }

  public void switchMode(int mode, boolean force) {
    if (mode == currentMode && !force) {
      return;
    }

    if (mode == STOPPED_MODE) {
      showStoppedMode();
    } else if (mode == PROMPT_DOWNLOAD_MODE) {
      showPromptForDownloadMode();
    } else if (mode == DOWNLOADING_MODE || mode == LOADING_MODE) {
      showProgress(mode);
    } else if (mode == PLAYING_MODE) {
      showPlayingMode(false);
    } else if (mode == PAUSED_MODE){
      showPlayingMode(true);
    } else if (mode == RECITATION_LISTENING_MODE){
      showRecitationListeningMode();
    } else if (mode == RECITATION_STOPPED_MODE){
      showRecitationStoppedMode();
    } else if (mode == RECITATION_PLAYING_MODE){
      showRecitationPlayingMode();
    }
  }

  @NonNull
  public QariItem getAudioInfo() {
    final int position = spinner != null ? spinner.getSelectedItemPosition() : currentQari;
    return adapter.getItem(position);
  }

  public void updateSelectedItem() {
    if (spinner != null) {
      spinner.setSelection(currentQari, false);
    }
  }

  public void setProgress(int progress) {
    if (hasErrorText) {
      progressText.setText(R.string.downloading_title);
      hasErrorText = false;
    }

    if (progressBar != null) {
      if (progress >= 0) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(progress);
        progressBar.setMax(100);
      } else {
        progressBar.setIndeterminate(true);
      }
    }
  }

  public void setProgressText(String progressText, boolean isCriticalError) {
    if (this.progressText != null) {
      hasErrorText = true;
      this.progressText.setText(progressText);
      if (isCriticalError && progressBar != null) {
        progressBar.setVisibility(View.GONE);
        this.progressText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            textFullFontSize);
        haveCriticalError = true;
      }
    }
  }

  /** Adds a fixed-height, forced-LTR horizontal row that add* helpers fill. */
  private LinearLayout newRow(int height) {
    LinearLayout row = new LeftToRightLinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    addView(row, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    currentRow = row;
    return row;
  }

  /** Current sura·ayah readout shown on the playing deck (mockup 18 B). */
  public void setNowPlayingInfo(CharSequence info) {
    nowPlayingInfo = info == null ? "" : info;
    if (nowPlayingText != null) {
      nowPlayingText.setText(nowPlayingInfo);
    }
  }

  private void showStoppedMode() {
    currentMode = STOPPED_MODE;
    removeAllViews();
    newRow(rowHeight);

    // mockup 18 A: reciter chip · name + status (tap = picker) · gold Play FAB
    if (isRtl) {
      if (isRecitationEnabled) {
        addButton(R.drawable.ic_mic, false);
      }
      addPlayFab(R.drawable.ic_play);
      addSpinner(adapter);
      addAvatarChip();
    } else {
      addAvatarChip();
      addSpinner(adapter);
      addPlayFab(R.drawable.ic_play);
      if (isRecitationEnabled) {
        addButton(R.drawable.ic_mic, false);
      }
    }
  }

  private static class QariAdapter extends BaseAdapter {
    @NonNull LayoutInflater inflater;
    @NonNull private final List<QariItem> items;
    @LayoutRes private final int layoutViewId;
    @LayoutRes private final int dropDownViewId;

    QariAdapter(@NonNull Context context,
                @NonNull List<QariItem> items,
                @LayoutRes int layoutViewId,
                @LayoutRes int dropDownViewId) {
      this.items = items;
      this.layoutViewId = layoutViewId;
      this.dropDownViewId = dropDownViewId;
      inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
      return items.size();
    }

    @Override
    public QariItem getItem(int position) {
      return items.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return getViewInternal(position, convertView, parent, layoutViewId);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return getViewInternal(position, convertView, parent, dropDownViewId);
    }

    private View getViewInternal(int position, View convertView,
        ViewGroup parent, @LayoutRes int resource) {
      final View view;
      if (convertView == null) {
        view = inflater.inflate(resource, parent, false);
      } else {
        view = convertView;
      }

      // the full (two-line) collapsed layout has a ViewGroup root; the name
      // TextView is @android:id/text1 in every variant
      final TextView textView = view instanceof TextView
          ? (TextView) view : view.findViewById(android.R.id.text1);
      QariItem item = getItem(position);
      textView.setText(item.getName());
      return view;
    }
  }

  private void addSpinner(QariAdapter target) {
    if (spinner == null) {
      spinner = new QuranSpinner(context, null,
          R.attr.actionDropDownStyle);
      // Unified navy dropdown skin (mockup 06b): navy card, gold-semi border,
      // gold band + check on the current value. Readable in both themes.
      spinner.setPopupBackgroundResource(R.drawable.bg_dropdown_navy);
      spinner.setDropDownVerticalOffset(spinnerPadding);
      // the collapsed item layouts draw their own gold caret
      spinner.setBackground(null);

      spinner.setOnItemSelectedListener(
          new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              if (position != currentQari) {
                sharedPreferences.edit().
                    putInt(Constants.PREF_DEFAULT_QARI, adapter.getItem(position).getId()).apply();
                currentQari = position;
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
          });
    }
    // the spinner instance is reused across modes/rows
    if (spinner.getParent() instanceof ViewGroup) {
      ((ViewGroup) spinner.getParent()).removeView(spinner);
    }
    if (spinner.getAdapter() != target) {
      spinner.setAdapter(target);
    }
    spinner.setSelection(currentQari);

    final LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
    params.weight = 1;
    if (isRtl) {
      ViewCompat.setLayoutDirection(spinner, ViewCompat.LAYOUT_DIRECTION_RTL);
    }
    params.leftMargin = separatorSpacing;
    params.rightMargin = separatorSpacing;
    currentRow.addView(spinner, params);
  }

  /** Gold-framed reciter identity chip at the reading start (mockup 18 .av). */
  private void addAvatarChip() {
    ImageView avatar = new ImageView(context);
    avatar.setImageResource(R.drawable.ic_mic);
    avatar.setColorFilter(
        androidx.core.content.ContextCompat.getColor(context, R.color.gold_light),
        android.graphics.PorterDuff.Mode.SRC_IN);
    avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    final int pad = avatarSize / 5;
    avatar.setPadding(pad, pad, pad, pad);
    avatar.setBackgroundResource(R.drawable.bg_audiobar_avatar);
    LayoutParams params = new LayoutParams(avatarSize, avatarSize);
    params.gravity = Gravity.CENTER_VERTICAL;
    params.leftMargin = spinnerPadding / 2;
    params.rightMargin = spinnerPadding / 2;
    currentRow.addView(avatar, params);
  }

  /** Round gold-gradient Play/Pause FAB (mockup 18 .play). */
  private void addPlayFab(int imageId) {
    ImageView fab = new ImageView(context);
    fab.setImageResource(imageId);
    fab.setColorFilter(
        androidx.core.content.ContextCompat.getColor(context, R.color.text_on_gold),
        android.graphics.PorterDuff.Mode.SRC_IN);
    fab.setScaleType(ImageView.ScaleType.CENTER);
    fab.setBackgroundResource(R.drawable.bg_play_fab);
    fab.setOnClickListener(onClickListener);
    fab.setOnLongClickListener(onLongClickListener);
    fab.setTag(imageId);
    LayoutParams params = new LayoutParams(fabSize, fabSize);
    params.gravity = Gravity.CENTER_VERTICAL;
    params.leftMargin = spinnerPadding / 2;
    params.rightMargin = spinnerPadding / 2;
    currentRow.addView(fab, params);
  }

  /** Small gold mic glyph leading the playing deck (mockup 18 B). */
  private void addDeckMic() {
    ImageView mic = new ImageView(context);
    mic.setImageResource(R.drawable.ic_mic);
    mic.setColorFilter(
        androidx.core.content.ContextCompat.getColor(context, R.color.gold_light),
        android.graphics.PorterDuff.Mode.SRC_IN);
    mic.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    final int size = deckHeight - separatorSpacing;
    LayoutParams params = new LayoutParams(size, size);
    params.gravity = Gravity.CENTER_VERTICAL;
    params.leftMargin = spinnerPadding / 2;
    params.rightMargin = 0;
    currentRow.addView(mic, params);
  }

  /** Gold-light "Surah · ayah N" readout on the playing deck (mockup 18 B). */
  private void addNowPlayingText() {
    nowPlayingText = new TextView(context);
    nowPlayingText.setTextColor(
        androidx.core.content.ContextCompat.getColor(context, R.color.gold_light));
    nowPlayingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFontSize);
    nowPlayingText.setTypeface(null, android.graphics.Typeface.BOLD);
    nowPlayingText.setGravity(Gravity.CENTER_VERTICAL);
    nowPlayingText.setSingleLine(true);
    nowPlayingText.setText(nowPlayingInfo);
    LayoutParams params = new LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
    params.leftMargin = spinnerPadding / 2;
    params.rightMargin = spinnerPadding / 2;
    currentRow.addView(nowPlayingText, params);
  }

  private void showPromptForDownloadMode() {
    currentMode = PROMPT_DOWNLOAD_MODE;

    removeAllViews();
    newRow(rowHeight);

    if (isRtl) {
      addButton(R.drawable.ic_cancel, false);
      addDownloadOver3gPrompt();
      addSeparator();
      addButton(R.drawable.ic_accept, false);
    } else {
      addButton(R.drawable.ic_accept, false);
      addSeparator();
      addDownloadOver3gPrompt();
      addButton(R.drawable.ic_cancel, false);
    }
  }

  private void addDownloadOver3gPrompt() {
    TextView mPromptText = new TextView(context);
    mPromptText.setTextColor(
        androidx.core.content.ContextCompat.getColor(context, R.color.text_on_navy));
    mPromptText.setGravity(Gravity.CENTER_VERTICAL);
    mPromptText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
        textFontSize);
    mPromptText.setText(R.string.download_non_wifi_prompt);
    LayoutParams params = new LayoutParams(0,
        LayoutParams.MATCH_PARENT);
    params.weight = 1;
    currentRow.addView(mPromptText, params);
  }

  private void showProgress(int mode) {
    currentMode = mode;

    removeAllViews();
    newRow(rowHeight);

    final int text = mode == DOWNLOADING_MODE ? R.string.downloading_title : R.string.index_loading;
    if (isRtl) {
      addDownloadProgress(text);
      addSeparator();
      addButton(R.drawable.ic_cancel, false);
    } else {
      addButton(R.drawable.ic_cancel, false);
      addSeparator();
      addDownloadProgress(text);
    }
  }

  private void addDownloadProgress(@StringRes int text) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);

    progressBar = (ProgressBar) LayoutInflater.from(context)
        .inflate(R.layout.download_progress_bar, currentRow, false);
    progressBar.setIndeterminate(true);
    progressBar.setVisibility(View.VISIBLE);

    ll.addView(progressBar, LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT);

    progressText = new TextView(context);
    progressText.setTextColor(
        androidx.core.content.ContextCompat.getColor(context, R.color.text_on_navy));
    progressText.setGravity(Gravity.CENTER_VERTICAL);
    progressText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFontSize);
    progressText.setText(text);

    ll.addView(progressText, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
    lp.weight = 1;
    lp.setMargins(separatorSpacing, 0, separatorSpacing, 0);
    if (isRtl) {
      lp.leftMargin = spinnerPadding;
    } else {
      lp.rightMargin = spinnerPadding;
    }
    currentRow.addView(ll, lp);
  }

  private void showRecitationListeningMode() {
    currentMode = RECITATION_LISTENING_MODE;
    removeAllViews();
    newRow(rowHeight);

    ImageView recitationButton = new ImageView(context);
    recitationButton.setImageTintList(ColorStateList.valueOf(Color.CYAN));

    if (isRtl) {
      addButton(recitationButton, R.drawable.ic_mic, false);
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_hide_page, false);
    } else {
      addButton(R.drawable.ic_hide_page, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addButton(recitationButton, R.drawable.ic_mic, false);
    }
  }

  private void showRecitationStoppedMode() {
    currentMode = RECITATION_STOPPED_MODE;
    removeAllViews();
    newRow(rowHeight);

    if (isRtl) {
      addButton(R.drawable.ic_mic, false);
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_play, false);
      addButton(R.drawable.ic_cancel, false);
    } else {
      addButton(R.drawable.ic_cancel, false);
      addButton(R.drawable.ic_play, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addButton(R.drawable.ic_mic, false);
    }
  }

  private void showRecitationPlayingMode() {
    currentMode = RECITATION_PLAYING_MODE;
    removeAllViews();
    newRow(rowHeight);

    if (isRtl) {
      addButton(R.drawable.ic_mic, false);
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_pause, false);
      addButton(R.drawable.ic_cancel, false);
    } else {
      addButton(R.drawable.ic_cancel, false);
      addButton(R.drawable.ic_pause, false);
      addSeparator();
      addSpacer();
      addSeparator();
      addButton(R.drawable.ic_transcript, false);
      addSeparator();
      addButton(R.drawable.ic_mic, false);
    }
  }

  private void showPlayingMode(boolean isPaused) {
    removeAllViews();

    final boolean withWeight = !isDualPageMode;

    int button;
    if (isPaused) {
      button = R.drawable.ic_play;
      currentMode = PAUSED_MODE;
    } else {
      button = R.drawable.ic_pause;
      currentMode = PLAYING_MODE;
    }

    // deck: reciter (tap = picker) + current sura·ayah readout (mockup 18 B)
    newRow(deckHeight);
    if (isRtl) {
      addNowPlayingText();
      addSpinner(deckAdapter);
      addDeckMic();
    } else {
      addDeckMic();
      addSpinner(deckAdapter);
      addNowPlayingText();
    }

    View hairline = new View(context);
    hairline.setBackgroundColor(0x1AFFFFFF);
    addView(hairline, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        Math.max(1, separatorWidth)));

    // controls: Stop · Prev · gold Play/Pause FAB · Next · Repeat ×N · Settings
    newRow(rowHeight);
    addButton(R.drawable.ic_stop, withWeight);
    addButton(R.drawable.ic_previous, withWeight);
    addPlayFab(button);
    addButton(R.drawable.ic_next, withWeight);

    addButton(repeatButton, R.drawable.ic_repeat, withWeight);
    updateRepeatButtonText();

    addButton(R.drawable.ic_action_settings, withWeight);
  }

  private void addButton(int imageId, boolean withWeight) {
    addButton(new ImageView(context), imageId, withWeight);
  }

  private void addButton(@NonNull ImageView button, int imageId, boolean withWeight) {
    if (button.getParent() instanceof ViewGroup) {
      // reused instances (e.g. the repeat button) must leave their old row
      ((ViewGroup) button.getParent()).removeView(button);
    }
    button.setImageResource(imageId);
    // Gold-accent the primary play/pause control and the affirmative Accept
    // check (mockup 18 variant D); other controls keep white-on-navy.
    if (imageId == R.drawable.ic_play || imageId == R.drawable.ic_pause
        || imageId == R.drawable.ic_accept) {
      button.setColorFilter(
          androidx.core.content.ContextCompat.getColor(context, R.color.gold_accent),
          android.graphics.PorterDuff.Mode.SRC_IN);
    } else {
      button.clearColorFilter();
    }
    button.setScaleType(ImageView.ScaleType.CENTER);
    button.setOnClickListener(onClickListener);
    button.setOnLongClickListener(onLongClickListener);
    button.setTag(imageId);
    button.setBackgroundResource(itemBackground);
    final LayoutParams params = new LayoutParams(
        withWeight ? 0 : buttonWidth, LayoutParams.MATCH_PARENT);
    if (withWeight) {
      params.weight = 1;
    }
    currentRow.addView(button, params);
  }

  private void addSeparator() {
    ImageView separator = new ImageView(context);
    separator.setBackgroundColor(0x38FFFFFF);  // soft translucent rule, not harsh full white
    separator.setPadding(0, separatorSpacing, 0, separatorSpacing);
    LinearLayout.LayoutParams paddingParams =
        new LayoutParams(separatorWidth, LayoutParams.MATCH_PARENT);

    final int right = isRtl ? 0 : separatorSpacing;
    final int left = isRtl ? separatorSpacing : 0;
    paddingParams.setMargins(left, 0, right, 0);
    currentRow.addView(separator, paddingParams);
  }

  private void addSpacer() {
    Space spacer = new Space(context);
    LinearLayout.LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
    params.weight = 1;
    currentRow.addView(spacer, params);
  }

  private void incrementRepeat() {
    currentRepeat++;
    if (currentRepeat - 1 == MAX_AUDIOBAR_QUICK_REPEAT) {
      currentRepeat = -1;
    } else if (currentRepeat > MAX_AUDIOBAR_QUICK_REPEAT) {
      currentRepeat = 0;
    }
    updateRepeatButtonText();
  }

  private void updateRepeatButtonText() {
    String str;
    if (currentRepeat == -1) {
      str = context.getString(R.string.infinity);
    } else if (currentRepeat == 0) {
      str = "";
    } else {
      str = String.valueOf(currentRepeat);
    }
    repeatButton.setText(str);
  }

  public void setRepeatCount(int repeatCount) {
    boolean updated = false;
    if (currentRepeat != repeatCount) {
      currentRepeat = repeatCount;
      updated = true;
    }

    if (updated && repeatButton != null) {
      updateRepeatButtonText();
    }
  }

  public void setAudioBarListener(AudioBarListener listener) {
    audioBarListener = listener;
  }

  public void setAudioBarRecitationListener(AudioBarRecitationListener listener) {
    audioBarRecitationListener = listener;
  }

  OnClickListener onClickListener = new OnClickListener() {
    @Override
    public void onClick(View view) {
      if (audioBarListener != null) {
        int tag = (Integer) view.getTag();
        switch (tag) {
          case R.drawable.ic_mic:
            audioBarRecitationListener.onRecitationPressed();
            break;
          case R.drawable.ic_transcript:
            audioBarRecitationListener.onRecitationTranscriptPressed();
            break;
          case R.drawable.ic_hide_page:
            audioBarRecitationListener.onHideVersesPressed();
            break;
          case R.drawable.ic_play:
            if (currentMode == RECITATION_STOPPED_MODE) {
              audioBarRecitationListener.onPlayRecitationPressed();
            } else {
              audioBarListener.onPlayPressed();
            }
            break;
          case R.drawable.ic_stop:
            audioBarListener.onStopPressed();
            break;
          case R.drawable.ic_pause:
            if (currentMode == RECITATION_PLAYING_MODE) {
              audioBarRecitationListener.onPauseRecitationPressed();
            } else {
              audioBarListener.onPausePressed();
            }
            break;
          case R.drawable.ic_next:
            audioBarListener.onNextPressed();
            break;
          case R.drawable.ic_previous:
            audioBarListener.onPreviousPressed();
            break;
          case R.drawable.ic_repeat:
            incrementRepeat();
            audioBarListener.setRepeatCount(currentRepeat);
            break;
          case R.drawable.ic_cancel:
            if (currentMode == RECITATION_STOPPED_MODE || currentMode == RECITATION_PLAYING_MODE) {
              audioBarRecitationListener.onEndRecitationSessionPressed();
            } else if (haveCriticalError) {
              haveCriticalError = false;
              switchMode(STOPPED_MODE);
            } else {
              audioBarListener.onCancelPressed(currentMode == DOWNLOADING_MODE);
            }
            break;
          case R.drawable.ic_accept:
            audioBarListener.onAcceptPressed();
            break;
          case R.drawable.ic_action_settings:
            audioBarListener.onAudioSettingsPressed();
            break;
        }
      }
    }
  };

  OnLongClickListener onLongClickListener = new OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
      if (audioBarListener != null) {
        int tag = (Integer) view.getTag();
        switch (tag) {
          case R.drawable.ic_mic:
            audioBarRecitationListener.onRecitationLongPressed();
            return true;
          case R.drawable.ic_transcript:
          case R.drawable.ic_play:
          case R.drawable.ic_stop:
          case R.drawable.ic_pause:
          case R.drawable.ic_next:
          case R.drawable.ic_previous:
          case R.drawable.ic_repeat:
          case R.drawable.ic_cancel:
          case R.drawable.ic_accept:
          case R.drawable.ic_action_settings:
          default:
            break;
        }
      }
      return false;
    }
  };

}
