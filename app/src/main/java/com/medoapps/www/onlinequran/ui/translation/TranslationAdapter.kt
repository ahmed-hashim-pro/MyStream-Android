package com.medoapps.www.onlinequran.ui.translation

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quran.data.model.SuraAyah
import com.quran.data.model.highlight.HighlightType
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.common.QuranAyahInfo
import com.medoapps.www.onlinequran.model.translation.ArabicDatabaseUtils
import com.medoapps.www.onlinequran.ui.helpers.ExpandTafseerSpan
import com.medoapps.www.onlinequran.ui.helpers.HighlightTypes
import com.medoapps.www.onlinequran.ui.helpers.InlineAyahMarkerSpan
import com.medoapps.www.onlinequran.ui.helpers.UthmaniSpan
import com.medoapps.www.onlinequran.ui.util.TypefaceManager
import com.medoapps.www.onlinequran.util.QuranSettings
import com.medoapps.www.onlinequran.util.QuranUtils
import com.medoapps.www.onlinequran.view.AyahNumberView
import com.medoapps.www.onlinequran.view.DividerView
import kotlin.math.ln1p
import kotlin.math.min

internal class TranslationAdapter(
  private val context: Context,
  private val recyclerView: RecyclerView,
  private val onClickListener: View.OnClickListener,
  private val onVerseSelectedListener: OnVerseSelectedListener
) : RecyclerView.Adapter<TranslationAdapter.RowViewHolder>() {
  private val inflater: LayoutInflater = LayoutInflater.from(context)
  private val data: MutableList<TranslationViewRow> = mutableListOf()

  private var fontSize: Int = 0
  private var textColor: Int = 0
  private var dividerColor: Int = 0
  private var arabicTextColor: Int = 0
  private var suraHeaderColor: Int = 0
  private var ayahSelectionColor: Int = 0
  private var isNightMode: Boolean = false

  // Amiri serif for the gold surah band (theme default is Cairo, which would win otherwise).
  private val amiriTypeface: android.graphics.Typeface? by lazy {
    androidx.core.content.res.ResourcesCompat.getFont(context, R.font.amiri_bold)
  }

  private var highlightedAyah: Int = 0
  private var highlightedRowCount: Int = 0
  private var highlightedStartPosition: Int = 0
  private var highlightType: HighlightType? = null

  private val expandedTafseerAyahs = mutableSetOf<Pair<Int, Int>>()
  private val expandedHyperlinks = mutableSetOf<Pair<Int, Int>>()

  private val defaultClickListener = View.OnClickListener { this.handleClick(it) }
  private val defaultLongClickListener = View.OnLongClickListener { this.selectVerseRows(it) }
  private val expandClickListener = View.OnClickListener { v -> toggleExpandTafseer(v) }
  private val expandHyperlinkClickListener = View.OnClickListener { v -> toggleExpandHyperlink(v) }

  fun getSelectedVersePopupPosition(): IntArray? {
    return if (highlightedStartPosition > -1) {
      val highlightedEndPosition = highlightedStartPosition + highlightedRowCount

      // anchor on the Arabic row of the highlighted ayah (the number is now inline in it);
      // fall back to the first highlighted row when there's no Arabic (e.g. arabic db absent)
      val anchorIndex = data.withIndex().firstOrNull {
        it.index in highlightedStartPosition until highlightedEndPosition &&
            it.value.type == TranslationViewRow.Type.QURAN_TEXT
      }?.index ?: highlightedStartPosition

      positionForViewHolderIndex(anchorIndex)
    } else {
      null
    }
  }

  fun getSelectedVersePopupPosition(sura: Int, ayah: Int): IntArray? {
    val (startPosition, _) = adapterInfoForAyah(sura, ayah)
    return if (startPosition > -1) {
      positionForViewHolderIndex(startPosition)
    } else {
      null
    }
  }

  private fun positionForViewHolderIndex(index: Int): IntArray? {
    val viewHolder = recyclerView.findViewHolderForAdapterPosition(index) as RowViewHolder?
      ?: return null
    // legacy: a dedicated ayah-number box (kept for safety, no longer emitted)
    viewHolder.ayahNumber?.let { ayahNumberView ->
      return intArrayOf(
        ayahNumberView.left + ayahNumberView.boxCenterX,
        ayahNumberView.top + ayahNumberView.boxBottomY
      )
    }
    // inline-number rows have no box: anchor at the row's horizontal center / bottom
    val row = viewHolder.itemView
    return intArrayOf(row.left + row.width / 2, row.top + row.height)
  }

  fun setData(data: List<TranslationViewRow>) {
    this.data.clear()
    expandedTafseerAyahs.clear()
    this.data.addAll(data)
    if (highlightedAyah > 0) {
      highlightAyah(highlightedAyah, true, highlightType ?: HighlightTypes.SELECTION, true)
    }
  }

  fun setHighlightedAyah(ayahId: Int, highlightType: HighlightType) {
    highlightAyah(ayahId, true, highlightType)
  }

  fun highlightedAyahInfo(): QuranAyahInfo? {
    return data.firstOrNull { it.ayahInfo.ayahId == highlightedAyah }?.ayahInfo
  }

  private fun adapterInfoForAyah(sura: Int, ayah: Int): Pair<Int, Int> {
    val matches =
      data.withIndex().filter {
        it.value.ayahInfo.sura == sura &&
            it.value.ayahInfo.ayah == ayah &&
            // don't factor in basmalah or sura name
            it.value.type > 1
      }
    return (matches.firstOrNull()?.index ?: -1) to matches.size
  }

  private fun highlightAyah(ayahId: Int, notify: Boolean, highlightedType: HighlightType, force: Boolean = false) {
    if (ayahId != highlightedAyah || force) {
      val matches = data.withIndex().filter { it.value.ayahInfo.ayahId == ayahId }
      val (startPosition, count) = (matches.firstOrNull()?.index ?: -1) to matches.size

      // highlight the newly highlighted ayah
      if (count > 0 && notify) {
        var startChangeCount = count
        var startChangeRange = startPosition
        if (highlightedRowCount > 0) {
          when {
            // merge the requests for notifyItemRangeChanged when we're either the next ayah
            highlightedStartPosition + highlightedRowCount + 1 == startPosition -> {
              startChangeRange = highlightedStartPosition
              startChangeCount += highlightedRowCount
            }
            // ... or when we're the previous ayah
            highlightedStartPosition - 1 == startPosition + count ->
              startChangeCount += highlightedRowCount
            else -> {
              // otherwise, unhighlight
              val start = highlightedStartPosition
              val changeCount = highlightedRowCount
              recyclerView.handler.post {
                notifyItemRangeChanged(start, changeCount, HIGHLIGHT_CHANGE)
              }
            }
          }
        }

        // and update rows to be highlighted
        recyclerView.handler.post {
          notifyItemRangeChanged(startChangeRange, startChangeCount, HIGHLIGHT_CHANGE)
          val layoutManager = recyclerView.layoutManager
          if ((force || highlightedType == HighlightTypes.AUDIO) && layoutManager is LinearLayoutManager) {
            layoutManager.scrollToPositionWithOffset(startPosition, 64)
          } else {
            recyclerView.smoothScrollToPosition(startPosition)
          }
        }
      }

      highlightedAyah = ayahId
      highlightedStartPosition = startPosition
      highlightedRowCount = count
      highlightType = highlightedType
    }
  }

  fun unhighlight() {
    if (highlightedAyah > 0 && highlightedRowCount > 0) {
      val start = highlightedStartPosition
      val count = highlightedRowCount
      recyclerView.handler.post {
        notifyItemRangeChanged(start, count)
      }
    }
    highlightedAyah = 0
    highlightedRowCount = 0
    highlightedStartPosition = -1
  }

  fun refresh(quranSettings: QuranSettings) {
    this.fontSize = quranSettings.translationTextSize
    isNightMode = quranSettings.isNightMode
    if (isNightMode) {
      val originalTextBrightness = quranSettings.nightModeTextBrightness
      val backgroundBrightness = quranSettings.nightModeBackgroundBrightness
      // avoid damaging the looks of the Quran page
      val adjustedBrightness = (50 * ln1p(backgroundBrightness.toDouble()) + originalTextBrightness).toInt()
      val textBrightness = min(adjustedBrightness.toFloat(), 255f).toInt()

      this.textColor = Color.rgb(textBrightness, textBrightness, textBrightness)
      this.arabicTextColor = textColor
      this.dividerColor = textColor
      this.suraHeaderColor = ContextCompat.getColor(context, R.color.translation_sura_header_night)
      this.ayahSelectionColor = ContextCompat.getColor(context, R.color.translation_ayah_selected_color_night)
    } else {
      this.textColor = ContextCompat.getColor(context, R.color.translation_text_color)
      this.dividerColor = ContextCompat.getColor(context, R.color.translation_divider_color)
      this.arabicTextColor = Color.BLACK
      this.suraHeaderColor = ContextCompat.getColor(context, R.color.translation_sura_header)
      this.ayahSelectionColor = ContextCompat.getColor(context, R.color.translation_ayah_selected_color)
    }

    if (this.data.isNotEmpty()) {
      notifyDataSetChanged()
    }
  }

  private fun handleClick(view: View) {
    val position = recyclerView.getChildAdapterPosition(view)
    if (highlightedAyah != 0 && position != RecyclerView.NO_POSITION) {
      val ayahInfo = data[position].ayahInfo
      if (ayahInfo.ayahId != highlightedAyah && highlightType == HighlightTypes.SELECTION) {
        onVerseSelectedListener.onVerseSelected(ayahInfo)
        return
      }
    }
    onClickListener.onClick(view)
  }

  private fun selectVerseRows(view: View): Boolean {
    val position = recyclerView.getChildAdapterPosition(view)
    if (position != RecyclerView.NO_POSITION) {
      val ayahInfo = data[position].ayahInfo
      highlightAyah(ayahInfo.ayahId, true, HighlightTypes.SELECTION)
      onVerseSelectedListener.onVerseSelected(ayahInfo)
      return true
    }
    return false
  }

  private fun toggleExpandTafseer(view: View) {
    val position = recyclerView.getChildAdapterPosition(view)
    if (position != RecyclerView.NO_POSITION) {
      val data = data[position]
      val what = data.ayahInfo.ayahId to data.translationIndex
      if (expandedTafseerAyahs.contains(what)) {
        expandedTafseerAyahs.remove(what)
      } else {
        expandedTafseerAyahs.add(what)
      }
      notifyItemChanged(position)
    }
  }

  private fun toggleExpandHyperlink(view: View) {
    val position = recyclerView.getChildAdapterPosition(view)
    if (position != RecyclerView.NO_POSITION) {
      val data = data[position]
      val what = data.ayahInfo.ayahId to data.translationIndex
      if (expandedHyperlinks.contains(what)) {
        expandedHyperlinks.remove(what)
      } else {
        expandedHyperlinks.add(what)
      }
      notifyItemChanged(position)
    }
  }

  override fun getItemViewType(position: Int): Int {
    return data[position].type
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
    @LayoutRes val layout = when (viewType) {
      TranslationViewRow.Type.SURA_HEADER -> R.layout.quran_translation_header_row
      TranslationViewRow.Type.BASMALLAH, TranslationViewRow.Type.QURAN_TEXT ->
        R.layout.quran_translation_arabic_row
      TranslationViewRow.Type.SPACER -> R.layout.quran_translation_spacer_row
      TranslationViewRow.Type.VERSE_NUMBER -> R.layout.quran_translation_verse_number_row
      TranslationViewRow.Type.TRANSLATOR -> R.layout.quran_translation_translator_row
      else -> R.layout.quran_translation_text_row
    }

    val view = inflater.inflate(layout, parent, false)
    return RowViewHolder(view)
  }

  override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
    val row = data[position]
    when {
      // a row with text
      holder.text != null -> {
        // reset click listener on the text
        holder.text.setOnClickListener(defaultClickListener)

        val text: CharSequence?
        if (row.type == TranslationViewRow.Type.SURA_HEADER) {
          text = row.data
          // Gold Amiri surah band (gold-faint box + border come from the row layout);
          // force Amiri over the theme's Cairo so it reads like the mockup .surah-band.
          amiriTypeface?.let { holder.text.typeface = it }
        } else if (row.type == TranslationViewRow.Type.BASMALLAH || row.type == TranslationViewRow.Type.QURAN_TEXT) {
          val arabic: CharSequence =
            if (row.type == TranslationViewRow.Type.BASMALLAH) {
              ArabicDatabaseUtils.AR_BASMALLAH
            } else {
              ArabicDatabaseUtils.getAyahWithoutBasmallah(
                row.ayahInfo.sura, row.ayahInfo.ayah, row.ayahInfo.arabicText
              )
            }
          val builder = SpannableStringBuilder(arabic)
          builder.setSpan(UthmaniSpan(context), 0, arabic.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

          // Inline ayah-end marker (mockup .mk) — gold-faint pill with the gold ayah
          // number, at the end of the Arabic line. Not on the basmala.
          if (row.type == TranslationViewRow.Type.QURAN_TEXT) {
            builder.append(' ')
            val markerStart = builder.length
            builder.append('￼')
            builder.setSpan(
              InlineAyahMarkerSpan(
                QuranUtils.getLocalizedNumber(context, row.ayahInfo.ayah),
                ContextCompat.getColor(context, R.color.gold_accent_faint),
                ContextCompat.getColor(context, R.color.gold_accent)
              ),
              markerStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
          }

          text = builder
          // Basmala in gold (mockup); ayah text keeps the paper-ink color.
          holder.text.setTextColor(
            if (row.type == TranslationViewRow.Type.BASMALLAH) {
              ContextCompat.getColor(context, R.color.gold_accent)
            } else {
              arabicTextColor
            }
          )
          holder.text.textSize = ARABIC_MULTIPLIER * fontSize
        } else {
          if (row.type == TranslationViewRow.Type.TRANSLATOR) {
            text = row.data
          } else {
            // translation
            text = row.data?.let { rowText ->
              val length = rowText.length
              val expandHyperlink =
                expandedHyperlinks.contains(row.ayahInfo.ayahId to row.translationIndex)

              if (row.link != null && !expandHyperlink) {
                holder.text.setOnClickListener(expandHyperlinkClickListener)
              }

              when {
                row.link != null && !expandHyperlink -> getAyahLink(row.link)
                length > MAX_TAFSEER_LENGTH ->
                  truncateTextIfNeeded(rowText, row.ayahInfo.ayahId, row.translationIndex)
                else -> rowText
              }
            }

            // determine text directionality
            val isRtl = when {
              row.isArabic -> true
              text != null -> QuranUtils.isRtl(text.toString())
              else -> false
            }

            // reset the typeface
            holder.text.typeface = null

            if (isRtl) {
              // rtl tafseer, style it (SDK is always >= 21 now)
              holder.text.layoutDirection = View.LAYOUT_DIRECTION_RTL

              // allow the tafseer font for api 19 because it's fine there and
              // is much better than the stock font (this is more lenient than
              // the api 21 restriction on the hafs font). only allow this for
              // Arabic though since the Arabic font isn't compatible with other
              // RTL languages that share some Arabic characters.
              // SDK is always >= 21 now
              if (row.isArabic) {
                holder.text.typeface = TypefaceManager.getTafseerTypeface(context)
              }
            } else {
              holder.text.layoutDirection = View.LAYOUT_DIRECTION_INHERIT
            }

            holder.text.movementMethod = LinkMovementMethod.getInstance()
            holder.text.setTextColor(textColor)
            holder.text.textSize = fontSize.toFloat()
          }
        }
        holder.text.text = text
      }
      // a divider row
      holder.divider != null -> {
        var showLine = true
        if (position + 1 < data.size) {
          val nextRow = data[position + 1]
          if (nextRow.ayahInfo.sura != row.ayahInfo.sura) {
            showLine = false
          }
        } else {
          showLine = false
        }
        holder.divider.toggleLine(showLine)
        holder.divider.setDividerColor(dividerColor)
      }
      // ayah number row
      holder.ayahNumber != null -> {
        val text = context.getString(R.string.sura_ayah, row.ayahInfo.sura, row.ayahInfo.ayah)
        holder.ayahNumber.setAyahString(text)
        // gold ayah marker (gold number on the gold-faint box), matching the mockup .mk
        holder.ayahNumber.setTextColor(ContextCompat.getColor(context, R.color.gold_accent))
        holder.ayahNumber.setNightMode(isNightMode)
      }
    }
    updateHighlight(row, holder)
  }

  private fun getAyahLink(link: SuraAyah): CharSequence {
    return context.getString(R.string.see_tafseer_of_verse, link.ayah)
  }

  private fun truncateTextIfNeeded(
    text: CharSequence,
    ayahId: Int,
    translationIndex: Int
  ): CharSequence {
    if (text.length > MAX_TAFSEER_LENGTH &&
      !expandedTafseerAyahs.contains(ayahId to translationIndex)
    ) {
      // let's truncate
      val lastSpace = text.indexOf(' ', MAX_TAFSEER_LENGTH)
      if (lastSpace != -1) {
        return SpannableStringBuilder(text.subSequence(0, lastSpace + 1)).apply {
          append(context.getString(R.string.more_arabic))
          setSpan(
            ExpandTafseerSpan(expandClickListener),
            lastSpace + 1,
            this.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
          )
        }
      }
    }
    return text
  }

  override fun onBindViewHolder(holder: RowViewHolder, position: Int, payloads: List<Any>) {
    if (payloads.contains(HIGHLIGHT_CHANGE)) {
      updateHighlight(data[position], holder)
    } else {
      super.onBindViewHolder(holder, position, payloads)
    }
  }

  private fun updateHighlight(row: TranslationViewRow, holder: RowViewHolder) {
    // toggle highlighting of the ayah, but not for sura headers and basmallah
    val isHighlighted = row.ayahInfo.ayahId == highlightedAyah
    if (row.type != TranslationViewRow.Type.SURA_HEADER &&
      row.type != TranslationViewRow.Type.BASMALLAH &&
      row.type != TranslationViewRow.Type.SPACER
    ) {
      holder.wrapperView.setBackgroundColor(
        if (isHighlighted) ayahSelectionColor else 0
      )
    } else if (holder.divider != null) { // SPACER type
      if (isHighlighted) {
        holder.divider.highlight(ayahSelectionColor)
      } else {
        holder.divider.unhighlight()
      }
    }
  }

  override fun getItemCount(): Int = data.size

  internal inner class RowViewHolder(val wrapperView: View) : RecyclerView.ViewHolder(wrapperView) {
    val text: TextView? = wrapperView.findViewById(R.id.text)
    val divider: DividerView? = wrapperView.findViewById(R.id.divider)
    val ayahNumber: AyahNumberView? = wrapperView.findViewById(R.id.ayah_number)

    init {
      wrapperView.setOnClickListener(defaultClickListener)
      wrapperView.setOnLongClickListener(defaultLongClickListener)
    }
  }

  internal interface OnVerseSelectedListener {
    fun onVerseSelected(ayahInfo: QuranAyahInfo)
  }

  companion object {
    const val ARABIC_MULTIPLIER = 1.4f
    private const val MAX_TAFSEER_LENGTH = 750
    private const val HIGHLIGHT_CHANGE = 1
  }
}
