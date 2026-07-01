package com.medoapps.www.onlinequran.ui.helpers

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.quran.data.model.bookmark.Tag
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.ui.QuranActivity
import com.medoapps.www.onlinequran.util.LocaleUtil
import com.medoapps.www.onlinequran.util.QuranUtils
import com.medoapps.www.onlinequran.view.JuzView
import com.medoapps.www.onlinequran.view.TagsViewGroup
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList

class QuranListAdapter @JvmOverloads constructor(
  private val context: Context,
  private val recyclerView: RecyclerView,
  private var elements: Array<QuranRow>,
  private val isEditable: Boolean,
  // Juz' tab: prefix the separator-band page with "صفحة"/"Page" (mockup 02).
  private val labelHeaderPage: Boolean = false
) : RecyclerView.Adapter<QuranListAdapter.HeaderHolder>(),
  View.OnClickListener, View.OnLongClickListener {

  private val inflater = LayoutInflater.from(context)
  private val checkedState = SparseBooleanArray()
  private val locale = LocaleUtil.getLocale(context)
  private var tagMap: Map<Long, Tag> = emptyMap()
  private var showTags = false
  private var showDate = false

  private var touchListener: QuranTouchListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderHolder {
    return if (viewType == 0) {
      HeaderHolder(inflater.inflate(R.layout.index_header_row, parent, false))
    } else {
      ViewHolder(inflater.inflate(R.layout.index_sura_row, parent, false))
    }
  }

  override fun onBindViewHolder(holder: HeaderHolder, position: Int) {
    val type = getItemViewType(position)
    return if (type == 0) bindHeader(holder, position) else bindRow(holder, position)
  }

  override fun getItemCount(): Int = elements.size

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getItemViewType(position: Int): Int {
    return if (elements[position].isHeader) 0 else 1
  }

  override fun onClick(v: View) {
    val position = recyclerView.getChildAdapterPosition(v)
    if (position != RecyclerView.NO_POSITION) {
      val element = elements[position]
      if (touchListener == null) {
        (context as QuranActivity).jumpTo(element.page)
      } else {
        touchListener?.onClick(element, position)
      }
    }
  }

  override fun onLongClick(v: View): Boolean {
    touchListener?.let { listener ->
      val position = recyclerView.getChildAdapterPosition(v)
      if (position != RecyclerView.NO_POSITION) {
        return listener.onLongClick(elements[position], position)
      }
    }
    return false
  }

  fun setElements(elements: Array<QuranRow>) {
    this.elements = elements
    notifyDataSetChanged()
  }

  fun isItemChecked(position: Int): Boolean = checkedState[position]

  fun setItemChecked(position: Int, checked: Boolean) {
    checkedState[position] = checked
    notifyItemChanged(position)
  }

  fun uncheckAll() {
    checkedState.clear()
    notifyDataSetChanged()
  }

  fun getCheckedItems(): List<QuranRow> {
    val result = ArrayList<QuranRow>()
    val count = checkedState.size()
    val elements = itemCount
    for (i in 0 until count) {
      val key = checkedState.keyAt(i)
      // TODO: figure out why sometimes elements > key
      if (checkedState[key] && elements > key) {
        result.add(getQuranRow(key))
      }
    }
    return result
  }

  fun setQuranTouchListener(listener: QuranTouchListener) {
    touchListener = listener
  }

  fun setElements(elements: Array<QuranRow>, tagMap: Map<Long, Tag>) {
    this.elements = elements
    this.tagMap = tagMap
  }

  fun setShowTags(showTags: Boolean) {
    this.showTags = showTags
  }

  fun setShowDate(showDate: Boolean) {
    this.showDate = showDate
  }

  private fun getQuranRow(position: Int): QuranRow = elements[position]

  // Set typefaces programmatically — the Mushaf theme's default Cairo fontFamily
  // silently overrides any view-level android:fontFamily, so XML fonts don't stick.
  private val amiriTypeface: android.graphics.Typeface? by lazy {
    androidx.core.content.res.ResourcesCompat.getFont(context, R.font.amiri_bold)
  }
  private val cairoExtraBold: android.graphics.Typeface? by lazy {
    androidx.core.content.res.ResourcesCompat.getFont(context, R.font.cairo_extrabold)
  }
  private val cairoBold: android.graphics.Typeface? by lazy {
    androidx.core.content.res.ResourcesCompat.getFont(context, R.font.cairo_bold)
  }

  private fun bindRow(vh: HeaderHolder, position: Int) {
    val holder = vh as ViewHolder
    bindHeader(vh, position)
    val item = elements[position]

    with(holder) {
      amiriTypeface?.let { title.typeface = it }
      number.text = QuranUtils.getLocalizedNumber(context, item.sura)
      metadata.visibility = View.VISIBLE
      metadata.text = item.metadata
      tags.visibility = View.GONE


      when {
        item.juzType != null -> {
          image.setImageDrawable(
            JuzView(context, item.juzType, item.juzOverlayText)
          )
          image.visibility = View.VISIBLE
          iconGlyph.visibility = View.GONE
          number.visibility = View.GONE
        }
        item.imageResource == null -> {
          number.visibility = View.VISIBLE
          image.visibility = View.GONE
          iconGlyph.visibility = View.GONE
        }
        else -> {
          // Bookmark / recent-page rows render the exact-UX glyph in a rounded chip
          // (mockup .row .ic): 📄 recent page · ♥ page bookmark · ★ ayah bookmark.
          image.visibility = View.GONE
          when {
            item.isAyahBookmark -> {
              // U+FE0E forces monochrome/text presentation so the star takes textColor
              iconGlyph.text = "★︎"
              iconGlyph.setBackgroundResource(R.drawable.bg_icon_frame_gold_solid)
              iconGlyph.setTextColor(ContextCompat.getColor(context, R.color.text_on_gold))
            }
            item.isBookmark -> {
              iconGlyph.text = "♥︎"
              iconGlyph.setBackgroundResource(R.drawable.bg_icon_frame_gold)
              iconGlyph.setTextColor(ContextCompat.getColor(context, R.color.gold_accent))
            }
            else -> {
              iconGlyph.text = "📄"
              iconGlyph.setBackgroundResource(R.drawable.bg_icon_frame_gold)
              iconGlyph.setTextColor(ContextCompat.getColor(context, R.color.gold_accent))
            }
          }
          iconGlyph.visibility = View.VISIBLE

          if (showDate) {
            val date = SimpleDateFormat("MMM dd, HH:mm", locale)
              .format(Date(item.dateAddedInMillis))
            holder.metadata.text = buildString {
              append(item.metadata)
              append(" - ")
              append(date)
            }
          }

          number.visibility = View.GONE

          val tagList = ArrayList<Tag>()
          val bookmark = item.bookmark
          if (bookmark != null && bookmark.tags.isNotEmpty() && showTags) {
            for (i in 0 until bookmark.tags.size) {
              val tagId = bookmark.tags[i]
              val tag = tagMap[tagId]
              tag?.let { tagList.add(it) }
            }
          }

          if (tagList.isEmpty()) {
            tags.visibility = View.GONE
          } else {
            tags.setTags(tagList)
            tags.visibility = View.VISIBLE
          }
        }
      }
    }
  }

  private fun bindHeader(holder: HeaderHolder, pos: Int) {
    val item = elements[pos]
    // Juz' band label = Cairo ExtraBold (800), page number = Cairo Bold (700).
    // (bindRow re-sets the title to Amiri afterwards for surah rows.)
    cairoExtraBold?.let { holder.title.typeface = it }
    cairoBold?.let { holder.pageNumber.typeface = it }
    // Bookmark tab bands are full-width squares (mockup .juzband override), unlike the
    // Surahs/Juz' inset pill. Guard to real headers — bindRow() also calls bindHeader().
    if (isEditable && item.isHeader) {
      holder.view.setBackgroundResource(R.drawable.bookmark_header_background)
    }
    holder.title.text = item.text
    when {
      // Tag-group band shows the count of bookmarks in the group on the trailing end.
      item.isBookmarkHeader && item.headerCount > 0 -> {
        holder.pageNumber.visibility = View.VISIBLE
        holder.pageNumber.text = QuranUtils.getLocalizedNumber(context, item.headerCount)
      }
      item.page == 0 -> holder.pageNumber.visibility = View.GONE
      else -> {
        holder.pageNumber.visibility = View.VISIBLE
        val pageNum = QuranUtils.getLocalizedNumber(context, item.page)
        // "صفحة N" only on the Juz' separator band; per-row page numbers stay bare.
        holder.pageNumber.text =
          if (labelHeaderPage && item.isHeader) {
            context.getString(R.string.juz_band_page) + " " + pageNum
          } else {
            pageNum
          }
      }
    }
    holder.setChecked(isItemChecked(pos))
    holder.setEnabled(isEnabled(pos))
  }

  private fun isEnabled(position: Int): Boolean {
    val selected = elements[position]
    return !isEditable ||                     // anything in surahs or juzs
        selected.isBookmark ||                // actual bookmarks
        selected.rowType == QuranRow.NONE ||  // the actual "current page"
        selected.isBookmarkHeader             // tags
  }

  open inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val view: View = itemView
    val title: TextView = itemView.findViewById(R.id.title)
    val pageNumber: TextView = itemView.findViewById(R.id.pageNumber)

    fun setEnabled(enabled: Boolean) {
      view.isEnabled = true
      itemView.setOnClickListener(
        if (enabled) this@QuranListAdapter else null
      )
      itemView.setOnLongClickListener(
        if (isEditable && enabled) this@QuranListAdapter else null
      )
    }

    fun setChecked(checked: Boolean) {
      view.isActivated = checked
    }
  }

  private inner class ViewHolder(itemView: View) : HeaderHolder(itemView) {
    val metadata: TextView = itemView.findViewById(R.id.metadata)
    val number: TextView = itemView.findViewById(R.id.suraNumber)
    val image: ImageView = itemView.findViewById(R.id.rowIcon)
    val iconGlyph: TextView = itemView.findViewById(R.id.rowIconGlyph)
    val tags: TagsViewGroup = itemView.findViewById(R.id.tags)
    val date: TextView? = itemView.findViewById(R.id.show_date)
  }

  interface QuranTouchListener {
    fun onClick(row: QuranRow, position: Int)
    fun onLongClick(row: QuranRow, position: Int): Boolean
  }
}
