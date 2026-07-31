package com.medoapps.www.onlinequran.more;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medoapps.www.onlinequran.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Screenshot-free UI testing. Question: can this repo render its XML off-device on the JVM
 * and assert measurable facts about the result, without a device and without a human
 * looking at a PNG?
 *
 * Every assertion here targets one of the six defects that shipped on the More page.
 *
 * Answer: yes. Measured on this machine (JBR 21.0.5, AGP 8.2.2, Gradle 8.5, Robolectric
 * 4.13, GraphicsMode.NATIVE, macOS arm64):
 *
 *   first run   30m29s  (~28m of it one-time downloads: nativeruntime-dist-compat
 *                        158MB + android-all-instrumented-14 151MB)
 *   warm run    13s     (8 tests, whole suite)
 *
 * What the run proved:
 *   - the whole screen inflates: Material3 theme via ContextThemeWrapper, CircleImageView,
 *     and com.google.android.gms.ads.AdView all survive (adView=true, header h=384px=128dp)
 *   - real RecyclerView + real MoreAdapter + real MoreUiState laid out 9 children
 *   - NATIVE graphics gives real text metrics: "Qibla Direction" measures 261.0px in a
 *     261px slot - EN is at the edge where AR ("اتجاه القبلة") is 145.5px. That is
 *     defect 4, quantified, and it is invisible if you only ever test Arabic.
 *   - drawing to a Bitmap and scanning a pixel column works: top pixel is exactly
 *     navy_700 (FF1F2A44), first hard transition to cream FFF8F0 at y=384 = the header
 *     bottom. That is the 8px-seam detector, automated.
 *
 * And it FAILED on a real, currently-shipping bug that no screenshot review caught:
 *   groupHeaderContrast_dark -> fg=FF121212 bg=FF121212 ratio=1.00
 *   item_more_header.xml uses android:textColor="@color/colorPrimaryDark", which
 *   values-night/colors.xml redefines to #121212 - the same value as
 *   background_main at night. Group headers are invisible in dark mode.
 *   design/specs/more-page.md sec.3 says this node must be ?attr/colorPrimary
 *   (= #D4A44C at night). This is exactly the "standing trap" the spec warns about.
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34, application = Application.class, qualifiers = "w412dp-h892dp-xxhdpi")
public class MorePageLayoutSpikeTest {

  private static final int WIDTH_DP = 412;
  private static final int HEIGHT_DP = 892;

  /** Tile labels that must survive a 4-up grid at 412dp. EN is the long locale. */
  private static final int[] TILE_LABELS = {
      R.string.asmaul_husna, R.string.dua_collection, R.string.fasting_tracker,
      R.string.hisn_al_muslim, R.string.islamic_events, R.string.livestrem,
      R.string.qibla_finder, R.string.zakat_calculator,
  };

  private Context themed() {
    Application app = RuntimeEnvironment.getApplication();
    return new ContextThemeWrapper(app, R.style.AppTheme);
  }

  private static int dp(Context c, float dp) {
    return Math.round(dp * c.getResources().getDisplayMetrics().density);
  }

  private View inflateAndLayout(Context c) {
    View root = LayoutInflater.from(c)
        .inflate(R.layout.activity_other_category_fragment, null, false);
    int w = View.MeasureSpec.makeMeasureSpec(dp(c, WIDTH_DP), View.MeasureSpec.EXACTLY);
    int h = View.MeasureSpec.makeMeasureSpec(dp(c, HEIGHT_DP), View.MeasureSpec.EXACTLY);
    root.measure(w, h);
    root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
    return root;
  }

  // ---------------------------------------------------------------- step 1

  /** Does the screen inflate at all: Material3 theme, AdView, CircleImageView. */
  @Test
  public void inflates() {
    Context c = themed();
    View root = inflateAndLayout(c);
    assertThat(root.getMeasuredWidth()).isEqualTo(dp(c, WIDTH_DP));
    System.out.println("SPIKE header h(px)=" + root.findViewById(R.id.moreHeader).getMeasuredHeight()
        + " adView=" + (root.findViewById(R.id.adView) != null));
  }

  // -------------------------------------------------- defect 5: 48dp targets

  @Test
  public void heroActionsMeet48dp() {
    Context c = themed();
    View root = inflateAndLayout(c);
    int min = dp(c, 48);
    for (int id : new int[] {R.id.moreRewards, R.id.moreSettings}) {
      View v = root.findViewById(id);
      System.out.println("SPIKE target " + v.getMeasuredWidth() + "x" + v.getMeasuredHeight()
          + " min=" + min);
      assertThat(v.getMeasuredWidth()).isAtLeast(min);
      assertThat(v.getMeasuredHeight()).isAtLeast(min);
    }
  }

  // -------------------------------------------- defect 4: EN-only label clip

  @Test
  public void tileLabelsFit_en() {
    assertTileLabelsFit(themed());
  }

  @Test
  @Config(qualifiers = "+ar-rSA-ldrtl")
  public void tileLabelsFit_ar() {
    assertTileLabelsFit(themed());
  }

  private void assertTileLabelsFit(Context c) {
    // 4 tiles across 412dp minus the list's 16dp side padding
    int cell = dp(c, (WIDTH_DP - 32) / 4f);
    List<String> clipped = new ArrayList<>();
    for (int res : TILE_LABELS) {
      View tile = LayoutInflater.from(c).inflate(R.layout.item_more_tile, null, false);
      TextView label = tile.findViewById(R.id.moreTileLabel);
      label.setText(res);
      tile.measure(View.MeasureSpec.makeMeasureSpec(cell, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
      tile.layout(0, 0, tile.getMeasuredWidth(), tile.getMeasuredHeight());
      int lines = label.getLineCount();
      int ellipsis = lines == 0 ? 0 : label.getLayout().getEllipsisCount(lines - 1);
      System.out.println("SPIKE label \"" + label.getText() + "\" lines=" + lines
          + " ellipsis=" + ellipsis + " w=" + (lines > 0 ? label.getLayout().getLineWidth(0) : -1)
          + " avail=" + (cell - label.getPaddingStart() - label.getPaddingEnd()));
      if (ellipsis > 0) clipped.add(String.valueOf(label.getText()));
    }
    assertThat(clipped).isEmpty();
  }

  // ------------------------------------------- defect 3: washed-out header

  @Test
  public void groupHeaderContrast_light() {
    assertHeaderContrast(themed());
  }

  @Test
  @Config(qualifiers = "+night")
  public void groupHeaderContrast_dark() {
    assertHeaderContrast(themed());
  }

  private void assertHeaderContrast(Context c) {
    View header = LayoutInflater.from(c).inflate(R.layout.item_more_header, null, false);
    TextView label = (TextView) header.findViewById(R.id.moreHeaderLabel);
    int fg = label.getCurrentTextColor();
    int bg = ContextCompat.getColor(c, R.color.background_main);
    double ratio = contrast(fg, bg);
    System.out.println(String.format("SPIKE header fg=%08X bg=%08X ratio=%.2f", fg, bg, ratio));
    assertThat(ratio).isAtLeast(4.5d);
  }

  private static double contrast(int fg, int bg) {
    double l1 = luminance(fg);
    double l2 = luminance(bg);
    double hi = Math.max(l1, l2);
    double lo = Math.min(l1, l2);
    return (hi + 0.05) / (lo + 0.05);
  }

  private static double luminance(int color) {
    double[] c = new double[3];
    int[] raw = {Color.red(color), Color.green(color), Color.blue(color)};
    for (int i = 0; i < 3; i++) {
      double v = raw[i] / 255d;
      c[i] = v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
  }

  // ----------------------------------- defect 6: content behind bottom nav

  @Test
  public void listBottomPaddingClearsFloatingDock() {
    Context c = themed();
    View root = inflateAndLayout(c);
    ViewGroup list = root.findViewById(R.id.listView);
    // content_main_activity.xml: BottomNavigationView 80dp + 12dp margin top & bottom
    int dockPx = dp(c, 80 + 12 + 12);
    System.out.println("SPIKE list paddingBottom=" + list.getPaddingBottom() + " dock=" + dockPx);
    assertThat(list.getPaddingBottom()).isAtLeast(dockPx);
    assertThat(list.getClipToPadding()).isFalse();
  }

  // ------------------------------- defect 2: cream seam under the status bar

  @Test
  public void noSeamAtTopOfHeader() {
    Context c = themed();
    View root = inflateAndLayout(c);
    Bitmap bmp = Bitmap.createBitmap(root.getMeasuredWidth(), root.getMeasuredHeight(),
        Bitmap.Config.ARGB_8888);
    root.draw(new Canvas(bmp));
    int navy = ContextCompat.getColor(c, R.color.navy_700);
    int x = bmp.getWidth() / 2;
    int firstTransition = -1;
    int prev = bmp.getPixel(x, 0);
    for (int y = 1; y < Math.min(bmp.getHeight(), dp(c, 260)); y++) {
      int p = bmp.getPixel(x, y);
      if (p != prev) {
        System.out.println(String.format("SPIKE column x=%d y=%d %08X -> %08X", x, y, prev, p));
        if (firstTransition < 0) firstTransition = y;
        prev = p;
      }
    }
    System.out.println(String.format("SPIKE top px=%08X navy=%08X firstTransition=%d headerH=%d",
        bmp.getPixel(x, 0), navy, firstTransition,
        root.findViewById(R.id.moreHeader).getMeasuredHeight()));
    // the very first row of the screen must already be the navy header, not a seam
    assertThat(bmp.getPixel(x, 0)).isEqualTo(navy);
  }

  // ------------------------------- real RecyclerView + real adapter + state

  @Test
  public void recyclerRendersRealAdapter() {
    Context c = themed();
    View root = inflateAndLayout(c);
    RecyclerView list = root.findViewById(R.id.listView);
    MoreAdapter adapter = new MoreAdapter(new MoreAdapter.Listener() {
      @Override public void onEntryClicked(MoreUiState.Entry e) { }
      @Override public void onContextCardClicked() { }
    });
    GridLayoutManager lm = new GridLayoutManager(c, MoreAdapter.SPAN_COUNT);
    lm.setSpanSizeLookup(adapter.spanSizeLookup(c));
    list.setLayoutManager(lm);
    list.setAdapter(adapter);
    adapter.submit(fixture());
    list.measure(
        View.MeasureSpec.makeMeasureSpec(list.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(list.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
    list.layout(0, 0, list.getMeasuredWidth(), list.getMeasuredHeight());
    System.out.println("SPIKE recycler children=" + list.getChildCount());
    assertThat(list.getChildCount()).isGreaterThan(0);

    // every clickable child must clear 48dp
    int min = dp(c, 48);
    for (int i = 0; i < list.getChildCount(); i++) {
      View child = list.getChildAt(i);
      if (child.isClickable()) {
        assertThat(child.getHeight()).isAtLeast(min);
      }
    }
  }

  private static MoreUiState fixture() {
    List<MoreUiState.Entry> entries = new ArrayList<>();
    for (int res : TILE_LABELS) {
      entries.add(new MoreUiState.Entry("id" + res, res, R.drawable.mystream,
          MoreUiState.Shape.TILE, null, null, null, false));
    }
    List<MoreUiState.Group> groups = new ArrayList<>(Arrays.asList(
        new MoreUiState.Group(R.string.more_group_tools, entries)));
    return new MoreUiState(null, groups, "", false);
  }
}
