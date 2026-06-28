package com.medoapps.www.onlinequran.ui

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.medoapps.www.onlinequran.R
import com.quran.data.core.QuranInfo
import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.common.audio.QariItem
import com.medoapps.www.onlinequran.util.AudioManagerUtils
import com.medoapps.www.onlinequran.util.AudioUtils
import com.medoapps.www.onlinequran.util.QariDownloadInfo
import com.medoapps.www.onlinequran.util.QuranFileUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.HashMap
import javax.inject.Inject

class AudioManagerActivity : AppCompatActivity() {
  private val disposable: CompositeDisposable = CompositeDisposable()

  private lateinit var progressBar: ProgressBar
  private lateinit var recyclerView: RecyclerView
  private lateinit var shuyookhAdapter: ShuyookhAdapter

  private var basePath: String? = null

  private var qariItems: List<QariItem> = emptyList()

  @Inject
  lateinit var audioUtils: AudioUtils

  @Inject
  lateinit var quranInfo: QuranInfo

  @Inject
  lateinit var quranFileUtils: QuranFileUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    val quranApp = application as QuranApplication
    quranApp.applicationComponent
        .inject(this)
    quranApp.refreshLocale(this, false)

    super.onCreate(savedInstanceState)

    setContentView(R.layout.audio_manager)
    applyNavyStatusBar()
    findViewById<ImageButton>(R.id.am_back)?.setOnClickListener { finish() }

    qariItems = audioUtils.getQariList(this)
    shuyookhAdapter = ShuyookhAdapter(qariItems)

    recyclerView = findViewById(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = shuyookhAdapter

    progressBar = findViewById(R.id.progress)
    basePath = quranFileUtils.getQuranAudioDirectory(this)
  }

  private fun requestShuyookhData() {
    disposable.clear()

    disposable.add(
        AudioManagerUtils.shuyookhDownloadObservable(quranInfo, basePath, qariItems)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ downloadInfo ->
              progressBar.visibility = View.GONE
              shuyookhAdapter.setDownloadInfo(downloadInfo)
              shuyookhAdapter.notifyDataSetChanged()
            }, { })
    )
  }

  override fun onResume() {
    super.onResume()
    requestShuyookhData()
  }

  override fun onDestroy() {
    disposable.dispose()
    super.onDestroy()
  }

  private val onClickListener =
    OnClickListener { v ->
      val position = recyclerView.getChildAdapterPosition(v)
      if (position != RecyclerView.NO_POSITION) {
        val qariItem = shuyookhAdapter.qariItems[position]
        val intent =
          Intent(this@AudioManagerActivity, SheikhAudioManagerActivity::class.java)
        intent.putExtra(SheikhAudioManagerActivity.EXTRA_SHEIKH, qariItem)
        startActivity(intent)
      }
    }

  /** Navy status bar with light icons, matching the rest of the Mushaf re-theme. */
  private fun applyNavyStatusBar() {
    window.statusBarColor = ContextCompat.getColor(this, R.color.navy_700)
    WindowCompat.getInsetsController(window, window.decorView)
        .isAppearanceLightStatusBars = false
  }

  private inner class ShuyookhAdapter(val qariItems: List<QariItem>) :
      Adapter<SheikhViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(this@AudioManagerActivity)
    private val downloadInfoMap: MutableMap<QariItem, QariDownloadInfo> = HashMap()

    fun setDownloadInfo(downloadInfo: List<QariDownloadInfo>) {
      for (info in downloadInfo) {
        downloadInfoMap[info.qariItem] = info
      }
    }

    override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
    ): SheikhViewHolder {
      return SheikhViewHolder(inflater.inflate(R.layout.audio_manager_row, parent, false))
    }

    override fun onBindViewHolder(
      holder: SheikhViewHolder,
      position: Int
    ) {
      holder.name.text = qariItems[position].name
      val info = getSheikhInfoForPosition(position)
      val fullyDownloaded = info!!.downloadedSuras.size()
      holder.quantity.text = resources.getQuantityString(
          R.plurals.files_downloaded,
          fullyDownloaded, fullyDownloaded
      )
      if (fullyDownloaded > 0) {
        holder.image.setBackgroundResource(R.drawable.downloaded_button_circle)
        holder.image.setImageResource(R.drawable.round_check_24)
      } else {
        holder.image.setBackgroundResource(R.drawable.download_button_circle)
        holder.image.setImageResource(R.drawable.ic_download)
      }
      holder.image.setColorFilter(
          ContextCompat.getColor(this@AudioManagerActivity, R.color.gold_accent),
          PorterDuff.Mode.SRC_IN
      )
    }

    fun getSheikhInfoForPosition(position: Int): QariDownloadInfo? {
      return downloadInfoMap[qariItems[position]]
    }

    override fun getItemCount(): Int {
      return if (downloadInfoMap.isEmpty()) 0 else qariItems.size
    }
  }

  private inner class SheikhViewHolder(itemView: View) :
      ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.name)
    val quantity: TextView = itemView.findViewById(R.id.quantity)
    val image: ImageView = itemView.findViewById(R.id.image)

    init {
      itemView.setOnClickListener(onClickListener)
    }
  }
}
