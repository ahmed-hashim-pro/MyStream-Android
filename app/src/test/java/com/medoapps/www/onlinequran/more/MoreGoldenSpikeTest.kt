package com.medoapps.www.onlinequran.more

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.common.truth.Truth.assertThat
import com.medoapps.www.onlinequran.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot-free UI testing, tier 2: does Roborazzi produce a golden PNG per variant,
 * and does adding it survive Kotlin 1.9.24 compilation of app/src/test?
 *
 * Record:  ./gradlew :app:testMadaniDebugUnitTest --tests '*MoreGoldenSpikeTest*' \
 *              -Droborazzi.test.record=true
 * Verify:  same, with -Droborazzi.test.verify=true
 *
 * Both answers are yes. Measured: record = 12s, verify = 9s, four PNGs in
 * app/build/screenshots/ (light/dark x en/ar). Kotlin 1.9.24 compiles against
 * roborazzi 1.44.0 without complaint — 1.46.0+ moves to kotlin-stdlib 2.0.21 and
 * would not.
 *
 * Two traps this spike hit, both worth keeping:
 *  1. `View.captureRoboImage()` throws "View should have Activity" on a detached
 *     view. The screen has to go through a real Robolectric Activity window.
 *  2. `getLayoutDirection()` reports LTR under the "ar-rSA-ldrtl" qualifier until
 *     the view has been measured+laid out. Asserting it before layout silently
 *     "passes" as LTR — i.e. an RTL check that never checks RTL.
 *
 * Fidelity caveat visible in more_light_ar.png: the two hero ImageButtons render
 * empty. They use app:srcCompat, which a plain android.app.Activity's inflater does
 * not honour. A golden shot through an AppCompat host would draw them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w412dp-h892dp-xxhdpi")
class MoreGoldenSpikeTest {

  /** Roborazzi needs a real window, so the screen goes into a Robolectric Activity. */
  private fun screen(): View {
    val controller = Robolectric.buildActivity(Activity::class.java).setup()
    val activity = controller.get()
    activity.setTheme(R.style.AppTheme)
    val root = LayoutInflater.from(activity)
      .inflate(R.layout.activity_other_category_fragment, null, false)
    activity.setContentView(root)
    val d = activity.resources.displayMetrics.density
    root.measure(
      View.MeasureSpec.makeMeasureSpec((412 * d).toInt(), View.MeasureSpec.EXACTLY),
      View.MeasureSpec.makeMeasureSpec((892 * d).toInt(), View.MeasureSpec.EXACTLY)
    )
    root.layout(0, 0, root.measuredWidth, root.measuredHeight)
    return root
  }

  @Test
  fun more_light_en() {
    screen().captureRoboImage("build/screenshots/more_light_en.png")
  }

  @Test
  @Config(qualifiers = "+night")
  fun more_dark_en() {
    screen().captureRoboImage("build/screenshots/more_dark_en.png")
  }

  @Test
  @Config(qualifiers = "+ar-rSA-ldrtl")
  fun more_light_ar() {
    // TRAP: getLayoutDirection() returns LTR until the view has been through a
    // measure/layout pass -- android:layoutDirection="locale" is resolved lazily.
    val root = screen()
    // layoutDirection="locale" must actually resolve to RTL, or EN/AR "coverage" is fake
    assertThat(root.layoutDirection).isEqualTo(View.LAYOUT_DIRECTION_RTL)
    root.captureRoboImage("build/screenshots/more_light_ar.png")
  }

  @Test
  @Config(qualifiers = "+ar-rSA-ldrtl-night")
  fun more_dark_ar() {
    screen().captureRoboImage("build/screenshots/more_dark_ar.png")
  }
}
