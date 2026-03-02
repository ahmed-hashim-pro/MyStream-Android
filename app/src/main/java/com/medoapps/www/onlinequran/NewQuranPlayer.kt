package com.medoapps.www.onlinequran

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.medoapps.www.onlinequran.util.AppBottomSheet
import com.medoapps.www.onlinequran.util.Config
import com.medoapps.www.onlinequran.util.SeparateFunctions
import java.io.File
import java.io.IOException
import java.util.HashMap

class NewQuranPlayer : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    companion object {
        const val Broadcast_PLAY_NEW_AUDIO = "com.medoapps.www.onlinequran.PlayNewAudio"
        const val Broadcast_updateProgressBarReceiver = "com.medoapps.www.onlinequran.updateProgressBarReceiver"

        @JvmField var btnPlay: ImageButton? = null
        @JvmField var isPlaying: Boolean? = false
        @JvmField var mp: MediaPlayer? = null
        @JvmField var songsList: ArrayList<HashMap<String, String>> = ArrayList()
        @JvmField var notificationTitle: String? = null
        @JvmField var ReciteNameText: String? = null
        @JvmField var mHandler: Handler = Handler()
        @JvmField var NewQuranPlayerInstance: NewQuranPlayer? = null
    }

    var m_objMediaSession: android.media.session.MediaSession? = null

    private val TAG = "managerdb"
    private var mAdView: AdView? = null

    lateinit var btnForward: ImageButton
    lateinit var btnBackward: ImageButton
    lateinit var btnNext: ImageButton
    lateinit var btnPrevious: ImageButton
    lateinit var btnBookmark: ImageButton
    lateinit var btnRepeat: ImageButton
    lateinit var btnShuffle: ImageButton
    lateinit var songProgressBar: SeekBar
    lateinit var songTitleLabel: TextView
    lateinit var songReciteName: TextView
    lateinit var sourceLabel: TextView
    lateinit var sourceBadge: LinearLayout
    lateinit var sourceRow: LinearLayout
    lateinit var sourceIcon: ImageView
    lateinit var btnDeleteSurah: ImageButton
    lateinit var songCurrentDurationLabel: TextView
    lateinit var songTotalDurationLabel: TextView
    lateinit var layoutads: LinearLayout

    private var wifiLock: android.net.wifi.WifiManager.WifiLock? = null

    lateinit var songManager: SongsManager
    lateinit var utils: Utilities
    @JvmField var seekForwardTime = 5000
    @JvmField var seekBackwardTime = 5000
    @JvmField var currentSongIndex = 0
    private var isShuffle = false
    private var isRepeat = false
    var RecitesName = ""
    var Rewayat = ""
    var RealRecitesName = ""
    var IsRadio: Boolean? = false
    var RecitesAYA = ""

    var isStartFromNotification: Boolean? = false
    var currentPlayerPosition = 0

    private val VISUALIZER_HEIGHT_DIP = 50f
    private var mVisualizer: Visualizer? = null
    private var mEqualizer: Equalizer? = null
    private var mLinearLayout: LinearLayout? = null
    private var mVisualizerView: VisualizerView? = null
    var scrollview: androidx.core.widget.NestedScrollView? = null
    private var equlizerstart = false

    private var player: MediaPlayerService? = null
    private var serviceMediaPlayer: ExoPlayer? = null
    var serviceBound = false
    private var isBoundToService = false
    var audioList: ArrayList<Audio>? = null

    var collapsingImageView: ImageView? = null

    var imageIndex = 0

    private var miniPlaylistRecyclerView: RecyclerView? = null
    private var miniPlaylistAdapter: MiniPlaylistAdapter? = null
    private var playlistLayoutManager: LinearLayoutManager? = null

    private val ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    private lateinit var refresh: Button
    private lateinit var btn_SHOWAD: Button
    private lateinit var startVideoAdsMuted: CheckBox
    private lateinit var videoStatus: TextView
    private var nativeAd: NativeAd? = null
    private lateinit var closeAd: ImageButton
    private lateinit var AdContainer: LinearLayout

    private lateinit var separateFunctions: SeparateFunctions

    private var numberOfTitleSet = 1
    private lateinit var loadingBar: LinearProgressIndicator

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val b = intent.extras
        if (b != null && b.getBoolean("isStartFromNotification", false)) {
            val storedIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (storedIndex >= 0 && songsList != null && storedIndex < songsList.size) {
                currentSongIndex = storedIndex
                numberOfTitleSet = 1
                if (miniPlaylistAdapter != null) {
                    miniPlaylistAdapter!!.setCurrentPlayingIndex(storedIndex)
                    miniPlaylistRecyclerView!!.post {
                        playlistLayoutManager!!.scrollToPositionWithOffset(
                            storedIndex, miniPlaylistRecyclerView!!.height / 3
                        )
                    }
                }
            }
        }
    }

    // Binding this Client to the AudioPlayer Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MediaPlayerService.LocalBinder
            player = binder.service
            serviceMediaPlayer = player!!.CurrentServiceExoPlayer()

            songProgressBar.progress = 0
            songProgressBar.progress = 0
            songProgressBar.max = 100

            if (player != null) {
                songTitleLabel.text = player!!.getTitle()
                songReciteName.text = player!!.getArtist()
                updateSourceLabel(player!!.getData())

                val serviceIndex = StorageUtil(applicationContext).loadAudioIndex()
                if (serviceIndex >= 0 && serviceIndex < songsList.size) {
                    currentSongIndex = serviceIndex
                    if (miniPlaylistAdapter != null) {
                        miniPlaylistAdapter!!.setCurrentPlayingIndex(serviceIndex)
                    }
                }
            }

            if (player!!.playbackStatusPublic == PlaybackStatus.PLAYING) {
                btnPlay!!.setImageResource(R.drawable.btn_pause)
            } else {
                btnPlay!!.setImageResource(R.drawable.btn_play)
            }

            serviceBound = true
            val storage = StorageUtil(applicationContext)
            storage.storeServiceBound(true)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun playAudio(audioIndex: Int) {
        if (isStartFromNotification == true) {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBoundToService = true
            updateProgressBar()
        } else {
            val storageo = StorageUtil(applicationContext)
            storageo.storeIsMediaStoppedFromUser(false)
            if (!serviceBound) {
                val storage = StorageUtil(applicationContext)
                storage.storeAudio(audioList)
                storage.storeAudioIndex(audioIndex)

                val playerIntent = Intent(this@NewQuranPlayer, MediaPlayerService::class.java)
                val activity: Activity = this@NewQuranPlayer
                startService(playerIntent)
                bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                isBoundToService = true

                try {
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                var isServiceNotFirstRun: Boolean
                isServiceNotFirstRun = storage.loadServiceBound()
                storage.storeAudioIndex(audioIndex)

                val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)
            } else {
                val storage = StorageUtil(applicationContext)
                storage.storeAudioIndex(audioIndex)

                val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)
            }
        }
        updateProgressBar()
    }

    private val updateProgressBarReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateProgressBar()
            hideLoading()
            if (player != null) {
                songTitleLabel.text = player!!.getTitle()
                songReciteName.text = player!!.getArtist()
                updateSourceLabel(player!!.getData())
                batteryOptimizationCheck()

                if (player!!.playbackStatusPublic == PlaybackStatus.PLAYING) {
                    btnPlay!!.setImageResource(R.drawable.btn_pause)
                } else {
                    btnPlay!!.setImageResource(R.drawable.btn_play)
                }

                val serviceIndex = StorageUtil(applicationContext).loadAudioIndex()
                if (serviceIndex != currentSongIndex && serviceIndex >= 0) {
                    currentSongIndex = serviceIndex
                    numberOfTitleSet = 1
                    updateBookmarkIcon()
                    if (miniPlaylistAdapter != null) {
                        miniPlaylistAdapter!!.setCurrentPlayingIndex(serviceIndex)
                    }
                }
            }
        }
    }

    private fun register_updateProgressBarReceiver() {
        try {
            unregisterReceiver(updateProgressBarReceiver)
        } catch (ignored: IllegalArgumentException) {
        }
        val filter = IntentFilter(Broadcast_updateProgressBarReceiver)
        try {
            ContextCompat.registerReceiver(this, updateProgressBarReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAudio() {
        audioList = ArrayList()

        for (map in songsList) {
            val songPathTxt = map["songPath"]
            val songTitleTxt = map["songTitle"]
            if (IsRadio == true) {
                audioList!!.add(Audio(songPathTxt, "$songTitleTxt-$RealRecitesName", "Ahmed HAshim", "Radio", RecitesName, Rewayat, RealRecitesName, RecitesAYA, IsRadio))
            } else {
                audioList!!.add(Audio(songPathTxt, songTitleTxt, "Ahmed HAshim", RealRecitesName, RecitesName, Rewayat, RealRecitesName, RecitesAYA, IsRadio))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBoundToService) {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBoundToService = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBoundToService) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isBoundToService = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mUpdateTimeTask)
        if (nativeAd != null) {
            nativeAd!!.destroy()
        }
        try {
            unregisterReceiver(updateProgressBarReceiver)
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView!!.mediaContent = nativeAd.mediaContent

        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView!!.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView!!.visibility = View.INVISIBLE
        } else {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView!!.visibility = View.INVISIBLE
        } else {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent!!.videoController

        if (vc.hasVideoContent()) {
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    refresh.isEnabled = true
                    videoStatus.text = "Video status: Video playback has ended."
                    super.onVideoEnd()
                }
            }
        } else {
            videoStatus.text = "Video status: Ad does not contain a video asset."
            refresh.isEnabled = true
        }
    }

    private fun refreshAd() {
        if (SettingSaved.isSubscribedPremium) return

        refresh.isEnabled = false

        val builder = AdLoader.Builder(this, getString(R.string.NATIVE_ADMOB_AD_UNIT_ID))

        builder.forNativeAd { nativeAd ->
            var isDestroyed = false
            refresh.isEnabled = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isDestroyed = isDestroyed()
            }
            if (isDestroyed || isFinishing || isChangingConfigurations) {
                nativeAd.destroy()
                return@forNativeAd
            }
            if (this@NewQuranPlayer.nativeAd != null) {
                this@NewQuranPlayer.nativeAd!!.destroy()
            }
            this@NewQuranPlayer.nativeAd = nativeAd
            val frameLayout = findViewById<FrameLayout>(R.id.fl_adplaceholder)
            val adView = layoutInflater.inflate(R.layout.ad_unified, null) as NativeAdView
            populateNativeAdView(nativeAd, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
            AdContainer.visibility = View.VISIBLE
        }

        val videoOptions = VideoOptions.Builder().setStartMuted(startVideoAdsMuted.isChecked).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)

        val adLoader = builder
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    refresh.isEnabled = true
                    val error = String.format(
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
        videoStatus.text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_quran_player)
        NewQuranPlayerInstance = this

        val settingSaved = SettingSaved(this@NewQuranPlayer)
        settingSaved.LoadData()

        separateFunctions = SeparateFunctions(this)

        MobileAds.initialize(this) { }

        loadingBar = findViewById(R.id.loadingBar)
        refresh = findViewById(R.id.btn_refresh)
        btn_SHOWAD = findViewById(R.id.btn_SHOWAD)
        closeAd = findViewById(R.id.closeAd)
        AdContainer = findViewById(R.id.AdContainer)
        startVideoAdsMuted = findViewById(R.id.cb_start_muted)
        videoStatus = findViewById(R.id.tv_video_status)

        AdContainer.visibility = View.GONE
        btn_SHOWAD.visibility = View.GONE

        closeAd.setOnClickListener {
            AdContainer.visibility = View.GONE
            btn_SHOWAD.visibility = View.VISIBLE
        }
        refresh.setOnClickListener {
            refreshAd()
        }
        btn_SHOWAD.setOnClickListener {
            refreshAd()
            btn_SHOWAD.visibility = View.GONE
        }

        refreshAd()

        val b = intent.extras
        if (b != null) {
            RecitesName = b.getString("RecitesName") ?: ""
            IsRadio = b.getBoolean("IsRadio")
            Rewayat = b.getString("Rewayat") ?: ""
            RealRecitesName = b.getString("RealRecitesName") ?: ""
            RecitesAYA = b.getString("RecitesAYA") ?: ""
            isStartFromNotification = b.getBoolean("isStartFromNotification")
            currentPlayerPosition = b.getInt("currentPlayerPosition")
        } else {
            finish()
        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        // All player buttons
        btnPlay = findViewById<ImageButton>(R.id.btnPlay)
        btnForward = findViewById(R.id.btnForward)
        btnBackward = findViewById(R.id.btnBackward)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)
        layoutads = findViewById(R.id.layoutads)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnShuffle = findViewById(R.id.btnShuffle)
        songProgressBar = findViewById(R.id.songProgressBar)
        songTitleLabel = findViewById(R.id.songTitle)
        songReciteName = findViewById(R.id.songrecite)
        sourceLabel = findViewById(R.id.sourceLabel)
        sourceBadge = findViewById(R.id.sourceBadge)
        sourceRow = findViewById(R.id.sourceRow)
        sourceIcon = findViewById(R.id.sourceIcon)
        btnDeleteSurah = findViewById(R.id.btnDeleteSurah)
        songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel)
        songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel)
        scrollview = findViewById(R.id.middleContent)
        btnBookmark = findViewById(R.id.bookmark)

        val lc = LnaguageClass(this@NewQuranPlayer)

        songTitleLabel = lc.SetTextFont(songTitleLabel, "")
        songReciteName = lc.SetTextFont(songReciteName, "")
        songTitleLabel.isSelected = true

        if (RealRecitesName != null && RealRecitesName.isNotEmpty()) {
            songReciteName.text = RealRecitesName
        }
        if (IsRadio == true) {
            btnRepeat.visibility = View.GONE
            btnShuffle.visibility = View.GONE
        }
        val storage = StorageUtil(applicationContext)

        if (storage.loadIsPlayerShuffle()) {
            btnShuffle.setImageResource(R.drawable.btn_shuffle_focused)
        }

        if (storage.loadIsPlayerRepeat()) {
            btnRepeat.setImageResource(R.drawable.btn_repeat_focused)
        }

        loadBannerAd()

        updateBookmarkIcon()

        songManager = SongsManager(this@NewQuranPlayer, this@NewQuranPlayer)
        utils = Utilities()

        songProgressBar.setOnSeekBarChangeListener(this)

        songsList = songManager.getPlayList(RecitesName, Rewayat, IsRadio)

        currentSongIndex = RecitesAYA.toInt()

        if (isStartFromNotification == true) {
            val storedIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (storedIndex >= 0 && storedIndex < songsList.size) {
                currentSongIndex = storedIndex
            }
        }

        loadAudio()
        showLoading()
        playAudio(currentSongIndex)
        btnPlay!!.setImageResource(R.drawable.btn_pause)

        if (songsList != null && currentSongIndex < songsList.size) {
            val songTitle = songsList[currentSongIndex]["songTitle"]
            val songPath = songsList[currentSongIndex]["songPath"]
            if (songTitle != null) songTitleLabel.text = songTitle
            updateSourceLabel(songPath)
        }

        // Set up mini-playlist
        miniPlaylistRecyclerView = findViewById(R.id.miniPlaylistRecyclerView)
        playlistLayoutManager = LinearLayoutManager(this)
        miniPlaylistRecyclerView!!.layoutManager = playlistLayoutManager
        miniPlaylistRecyclerView!!.isNestedScrollingEnabled = false

        miniPlaylistAdapter = MiniPlaylistAdapter(songsList, this) { position ->
            onMiniPlaylistItemClicked(position)
        }
        miniPlaylistRecyclerView!!.adapter = miniPlaylistAdapter
        miniPlaylistAdapter!!.setCurrentPlayingIndex(currentSongIndex)

        miniPlaylistRecyclerView!!.post {
            playlistLayoutManager!!.scrollToPositionWithOffset(
                currentSongIndex, miniPlaylistRecyclerView!!.height / 3
            )
        }

        register_updateProgressBarReceiver()

        if (player != null) {
            player!!.STATE_PLAYING_public()
        }

        /**
         * Play button click event
         */
        btnPlay!!.setOnClickListener {
            if (player != null && player!!.CurrentServiceExoPlayer() != null) {
                if (player!!.playbackStatusPublic == PlaybackStatus.PLAYING) {
                    player!!.PausePublic()
                    btnPlay!!.setImageResource(R.drawable.btn_play)
                } else if (player!!.playbackStatusPublic == PlaybackStatus.PAUSED) {
                    player!!.ResumePublic()
                    btnPlay!!.setImageResource(R.drawable.btn_pause)
                }
            } else {
                stopMediaPlayerService()
                loadAndPlayAudio()
            }
        }

        /**
         * Forward button click event
         */
        btnForward.setOnClickListener {
            if (player != null && player!!.CurrentServiceExoPlayer() != null) {
                val currentPosition = serviceMediaPlayer!!.currentPosition
                val duration = serviceMediaPlayer!!.duration
                if (duration != C.TIME_UNSET) {
                    if (currentPosition + seekForwardTime <= duration) {
                        serviceMediaPlayer!!.seekTo(currentPosition + seekForwardTime.toLong())
                    } else {
                        serviceMediaPlayer!!.seekTo(duration)
                    }
                }
            } else {
                stopMediaPlayerService()
                loadAndPlayAudio()
            }
        }

        /**
         * Backward button click event
         */
        btnBackward.setOnClickListener {
            if (player != null && player!!.CurrentServiceExoPlayer() != null) {
                val currentPosition = serviceMediaPlayer!!.currentPosition
                if (currentPosition - seekBackwardTime >= 0) {
                    serviceMediaPlayer!!.seekTo(currentPosition - seekBackwardTime.toLong())
                } else {
                    serviceMediaPlayer!!.seekTo(0L)
                }
            } else {
                stopMediaPlayerService()
                loadAndPlayAudio()
            }
        }

        /**
         * Next button click event
         */
        btnNext.setOnClickListener {
            if (player != null && player!!.CurrentServiceExoPlayer() != null) {
                showLoading()
                player!!.SkipToNextPublic()
                player!!.STATE_PLAYING_public()
            } else {
                showLoading()
                stopMediaPlayerService()
                loadAndPlayAudio()
            }
        }

        /**
         * Back button click event
         */
        btnPrevious.setOnClickListener {
            if (player != null && player!!.CurrentServiceExoPlayer() != null) {
                showLoading()
                player!!.SkipToPreviousPublic()
            } else {
                showLoading()
                stopMediaPlayerService()
                loadAndPlayAudio()
            }
        }

        /**
         * Button Click event for Repeat button
         */
        btnRepeat.setOnClickListener {
            val storage = StorageUtil(applicationContext)
            storage.storeServiceBound(true)
            if (storage.loadIsPlayerRepeat()) {
                storage.storeIsPlayerRepeat(false)
                isRepeat = false
                btnRepeat.setImageResource(R.drawable.btn_repeat)
            } else {
                storage.storeIsPlayerRepeat(true)
                isRepeat = true
                storage.storeIsPlayerShuffle(false)
                isShuffle = false
                btnRepeat.setImageResource(R.drawable.btn_repeat_focused)
                btnShuffle.setImageResource(R.drawable.btn_shuffle)
            }
        }

        /**
         * Button Click event for Shuffle button
         */
        btnShuffle.setOnClickListener {
            val storage = StorageUtil(applicationContext)
            if (storage.loadIsPlayerShuffle()) {
                storage.storeIsPlayerShuffle(false)
                isShuffle = false
                btnShuffle.setImageResource(R.drawable.btn_shuffle)
            } else {
                storage.storeIsPlayerShuffle(true)
                isShuffle = true
                storage.storeIsPlayerRepeat(false)
                isRepeat = false
                btnShuffle.setImageResource(R.drawable.btn_shuffle_focused)
                btnRepeat.setImageResource(R.drawable.btn_repeat)
            }
        }

        /**
         * Button Click event for Bookmark
         */
        btnBookmark.setOnClickListener {
            val currentAya = currentSongIndex.toString()
            var surahTitle = ""
            try {
                surahTitle = songsList[currentSongIndex]["songTitle"] ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (SettingSaved.isBookmarked(RecitesName, currentAya)) {
                SettingSaved.removeBookmark(applicationContext, RecitesName, currentAya)
                btnBookmark.setImageResource(R.drawable.round_bookmark_border_24)
                Toast.makeText(applicationContext, R.string.bookmark_removed, Toast.LENGTH_SHORT).show()
            } else {
                SettingSaved.addBookmark(applicationContext, RecitesName, currentAya, Rewayat, RealRecitesName, surahTitle)
                btnBookmark.setImageResource(R.drawable.round_bookmark_filled_24)
                Toast.makeText(applicationContext, R.string.bookmark_added, Toast.LENGTH_SHORT).show()
            }

            SettingSaved.FinalRecite = RecitesName
            SettingSaved.FinalAya = currentAya
            SettingSaved.FinalRewayat = Rewayat
            SettingSaved.FinalRealRecitesName = RealRecitesName
        }
    }

    private fun updateBookmarkIcon() {
        val currentAya = currentSongIndex.toString()
        if (SettingSaved.isBookmarked(RecitesName, currentAya)) {
            btnBookmark.setImageResource(R.drawable.round_bookmark_filled_24)
        } else {
            btnBookmark.setImageResource(R.drawable.round_bookmark_border_24)
        }
    }

    private fun loadBannerAd() {
        if (SettingSaved.isSubscribedPremium) return

        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)
        mAdView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView!!.visibility = View.VISIBLE
            }
        }
    }

    fun stopMediaPlayerService() {
        serviceBound = false
        try {
            unbindService(serviceConnection)
            player!!.stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAndPlayAudio() {
        loadAudio()
        playAudio(currentSongIndex)
    }

    private fun onMiniPlaylistItemClicked(position: Int) {
        if (position < 0 || position >= songsList.size) return
        currentSongIndex = position
        showLoading()

        val songTitle = songsList[position]["songTitle"]
        val songPath = songsList[position]["songPath"]
        if (songTitle != null) songTitleLabel.text = songTitle
        updateSourceLabel(songPath)

        val storage = StorageUtil(applicationContext)
        storage.storeAudioIndex(position)

        if (player != null && player!!.CurrentServiceExoPlayer() != null) {
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            broadcastIntent.setPackage(packageName)
            sendBroadcast(broadcastIntent)
        } else {
            stopMediaPlayerService()
            loadAndPlayAudio()
        }

        if (miniPlaylistAdapter != null) {
            miniPlaylistAdapter!!.setCurrentPlayingIndex(position)
        }
        btnPlay!!.setImageResource(R.drawable.btn_pause)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_managerdb, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.gbackmenu) {
            this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 100) {
            currentSongIndex = data!!.extras!!.getInt("songIndex")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*
            if(mp.isPlaying())
                if(mp!=null)
                    mp.pause();

            this.finish();
            nManager.cancel(2);
            */
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     */
    private fun showLoading() {
        loadingBar.visibility = View.VISIBLE
        songProgressBar.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        loadingBar.visibility = View.GONE
        songProgressBar.visibility = View.VISIBLE
    }

    private fun updateSourceLabel(path: String?) {
        if (!::sourceLabel.isInitialized || !::sourceBadge.isInitialized || !::sourceIcon.isInitialized || !::sourceRow.isInitialized) return
        sourceRow.visibility = View.VISIBLE
        if (path != null && path.startsWith("http")) {
            sourceLabel.text = getString(R.string.source_streaming)
            sourceLabel.setTextColor(resources.getColor(R.color.gold_accent))
            sourceIcon.setImageResource(R.drawable.ic_streaming)
            sourceBadge.setBackgroundResource(R.drawable.badge_streaming)
            btnDeleteSurah.visibility = View.GONE
        } else {
            sourceLabel.text = getString(R.string.source_offline)
            sourceLabel.setTextColor(0xFF4CAF50.toInt())
            sourceIcon.setImageResource(R.drawable.ic_offline)
            sourceBadge.setBackgroundResource(R.drawable.badge_offline)
            btnDeleteSurah.visibility = View.VISIBLE
            btnDeleteSurah.setOnClickListener {
                confirmDeleteCurrentSurah()
            }
        }
    }

    private fun confirmDeleteCurrentSurah() {
        if (songsList == null || currentSongIndex >= songsList.size) return
        val songPath = songsList[currentSongIndex]["songPath"]
        if (songPath == null || songPath.startsWith("http")) return

        val songTitle = songsList[currentSongIndex]["songTitle"]
        AppBottomSheet.showConfirmation(
            this,
            getString(R.string.audio_manager_surah_delete),
            getString(R.string.audio_manager_remove_audio_msg, songTitle),
            getString(android.R.string.yes),
            getString(android.R.string.no),
            { deleteCurrentSurah(songPath) }, null
        )
    }

    private fun deleteCurrentSurah(filePath: String) {
        try {
            val file = File(filePath)
            var deleted = false
            if (file.exists()) {
                deleted = file.delete()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val resolver = contentResolver
                    val selection = MediaStore.Audio.Media.DATA + "=?"
                    val selectionArgs = arrayOf(filePath)
                    resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (deleted || !file.exists()) {
                val lc = LnaguageClass(this@NewQuranPlayer, this@NewQuranPlayer)
                var serverName = ""
                val fileName = file.name
                if (fileName.endsWith(".mp3") && fileName.startsWith(RecitesName)) {
                    serverName = fileName.substring(RecitesName.length, fileName.length - 4)
                }

                val streamUrl: String
                if (Rewayat != null && Rewayat.isNotEmpty()) {
                    streamUrl = "https://server${lc.serverNumber(RecitesName)}.mp3quran.net/$RecitesName/$Rewayat/$serverName.mp3"
                } else {
                    streamUrl = "https://server${lc.serverNumber(RecitesName)}.mp3quran.net/$RecitesName/$serverName.mp3"
                }

                songsList[currentSongIndex]["songPath"] = streamUrl

                loadAudio()
                val storage = StorageUtil(applicationContext)
                storage.storeAudio(audioList)
                storage.storeAudioIndex(currentSongIndex)

                val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)

                updateSourceLabel(streamUrl)

                Toast.makeText(this, getString(R.string.surah_deleted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.surah_delete_failed), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.surah_delete_failed), Toast.LENGTH_SHORT).show()
        }
    }

    fun playSong(songIndex: Int) {
        try {
            mp!!.reset()
            mp!!.setDataSource(songsList[songIndex]["songPath"])
            mp!!.prepare()
            mp!!.start()

            SettingSaved.isfullscreenadshow = true
            val settingSaved = SettingSaved(applicationContext)
            settingSaved.SaveData()
            settingSaved.LoadData()

            val songTitle = songsList[songIndex]["songTitle"]
            val songPath = songsList[songIndex]["songPath"]
            songTitleLabel.text = songTitle
            notificationTitle = songTitle
            ReciteNameText = RealRecitesName
            songReciteName.text = ReciteNameText

            updateSourceLabel(songPath)
            NotificationPanel.ubdateNotification()

            if (!equlizerstart) {
                // hashim close the visulizer
                /*
                mEqualizer = new Equalizer(0, mp.getAudioSessionId());
                mEqualizer.setEnabled(true);
                setupVisualizerFxAndUI();
                setupEqualizerFxAndUI();
                mVisualizer.setEnabled(true);
                */
            } else {
            }

            autoscroll()

            btnPlay!!.setImageResource(R.drawable.btn_pause)

            songProgressBar.progress = 0
            songProgressBar.max = 100

            updateProgressBar()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        checkRating()
        super.onBackPressed()
    }

    fun checkRating() {
        Log.d(TAG, "checkRating: ")
        val settingSaved = SettingSaved(this@NewQuranPlayer)
        settingSaved.LoadData()

        Log.d(TAG, "checkRating: ${SettingSaved.numberOFBackClicksForIntent}")
        if (SettingSaved.numberOFBackClicksForIntent == Config.numberOFBackClicksForRating) {
            separateFunctions.rateAppInAppReview(this@NewQuranPlayer)
            SettingSaved.numberOFBackClicksForIntent = SettingSaved.numberOFBackClicksForIntent + 1
            settingSaved.SaveData()
        } else {
            SettingSaved.numberOFBackClicksForIntent = SettingSaved.numberOFBackClicksForIntent + 1
            settingSaved.SaveData()
            finish()
        }
    }

    private fun batteryOptimizationCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                separateFunctions.showNewCustomDialog(
                    getString(R.string.BatteryOptimizationTitle),
                    getString(R.string.BatteryOptimizationDialog),
                    getString(android.R.string.yes),
                    getString(android.R.string.no),
                    showAndroidSystematteryOptimizationRunnable,
                    android.R.drawable.ic_dialog_info
                )
            }
        }
    }

    val showAndroidSystematteryOptimizationRunnable = Runnable {
        showAndroidSystematteryOptimization()
    }

    private fun showAndroidSystematteryOptimization() {
        val intent = Intent()
        val packageName = packageName
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /**
     * Update timer on seekbar
     */
    fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    /**
     * Background Runnable thread
     */
    val mUpdateTimeTask = object : Runnable {
        override fun run() {
            try {
                val totalDuration = serviceMediaPlayer!!.duration
                if (totalDuration == C.TIME_UNSET) {
                    mHandler.postDelayed(this, 100)
                    return
                }
                val currentDuration = serviceMediaPlayer!!.currentPosition

                songTotalDurationLabel.text = "" + utils.milliSecondsToTimer(totalDuration)
                songCurrentDurationLabel.text = "" + utils.milliSecondsToTimer(currentDuration)

                if (player != null) {
                    if (numberOfTitleSet < 4) {
                        numberOfTitleSet = numberOfTitleSet + 1
                        songTitleLabel.text = player!!.getTitle()
                        songReciteName.text = player!!.getArtist()
                    }
                }

                val progress = utils.getProgressPercentage(currentDuration, totalDuration).toInt()
                songProgressBar.progress = progress

                mHandler.postDelayed(this, 100)
                if (currentDuration >= totalDuration / 8) {
                    layoutads.visibility = View.VISIBLE
                }
            } catch (ex: Exception) {
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask)
            val totalDuration = serviceMediaPlayer!!.duration
            if (totalDuration == C.TIME_UNSET) return
            val currentPosition = utils.progressToTimer(seekBar.progress, totalDuration.toInt())
            serviceMediaPlayer!!.seekTo(currentPosition.toLong())
            updateProgressBar()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun loadad() {
        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });*/
    }

    private fun equalizeSound() {
        val equalizerPresetNames = ArrayList<String>()
        val equalizerPresetSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            equalizerPresetNames
        )
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val equalizerPresetSpinner = findViewById<Spinner>(R.id.spinner)

        for (i in 0 until mEqualizer!!.numberOfPresets) {
            equalizerPresetNames.add(mEqualizer!!.getPresetName(i.toShort()))
        }

        equalizerPresetSpinner.adapter = equalizerPresetSpinnerAdapter

        equalizerPresetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mEqualizer!!.usePreset(position.toShort())
                val numberFrequencyBands = mEqualizer!!.numberOfBands
                val lowerEqualizerBandLevel = mEqualizer!!.bandLevelRange[0]

                for (i in 0 until numberFrequencyBands) {
                    val equalizerBandIndex = i.toShort()
                    val seekBar = findViewById<SeekBar>(equalizerBandIndex.toInt())
                    seekBar.progress = mEqualizer!!.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupEqualizerFxAndUI() {
        mLinearLayout = findViewById(R.id.linearLayoutEqual)

        val equalizerHeading = TextView(this)
        equalizerHeading.text = "Equalizer"
        equalizerHeading.textSize = 20f
        equalizerHeading.gravity = Gravity.CENTER_HORIZONTAL
        mLinearLayout!!.addView(equalizerHeading)

        val numberFrequencyBands = mEqualizer!!.numberOfBands
        val lowerEqualizerBandLevel = mEqualizer!!.bandLevelRange[0]
        val upperEqualizerBandLevel = mEqualizer!!.bandLevelRange[1]

        for (i in 0 until numberFrequencyBands) {
            val equalizerBandIndex = i.toShort()

            val frequencyHeaderTextview = TextView(this)
            frequencyHeaderTextview.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            frequencyHeaderTextview.gravity = Gravity.CENTER_HORIZONTAL
            frequencyHeaderTextview.text = "${mEqualizer!!.getCenterFreq(equalizerBandIndex) / 1000} Hz"
            mLinearLayout!!.addView(frequencyHeaderTextview)

            val seekBarRowLayout = LinearLayout(this)
            seekBarRowLayout.orientation = LinearLayout.HORIZONTAL

            val lowerEqualizerBandLevelTextview = TextView(this)
            lowerEqualizerBandLevelTextview.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lowerEqualizerBandLevelTextview.text = "${lowerEqualizerBandLevel / 100} dB"

            val upperEqualizerBandLevelTextview = TextView(this)
            upperEqualizerBandLevelTextview.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            upperEqualizerBandLevelTextview.text = "${upperEqualizerBandLevel / 100} dB"

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1f

            val seekBar = SeekBar(this)
            seekBar.id = i
            seekBar.layoutParams = layoutParams
            seekBar.max = upperEqualizerBandLevel - lowerEqualizerBandLevel
            seekBar.progress = mEqualizer!!.getBandLevel(equalizerBandIndex).toInt()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mEqualizer!!.setBandLevel(equalizerBandIndex, (progress + lowerEqualizerBandLevel).toShort())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            seekBarRowLayout.addView(lowerEqualizerBandLevelTextview)
            seekBarRowLayout.addView(seekBar)
            seekBarRowLayout.addView(upperEqualizerBandLevelTextview)

            mLinearLayout!!.addView(seekBarRowLayout)

            equalizeSound()
        }
    }

    private fun setupVisualizerFxAndUI() {
        mLinearLayout = findViewById(R.id.linearLayoutVisual)
        mVisualizerView = VisualizerView(this)
        mVisualizerView!!.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (VISUALIZER_HEIGHT_DIP * resources.displayMetrics.density).toInt()
        )
        mLinearLayout!!.addView(mVisualizerView)

        mVisualizer = Visualizer(mp!!.audioSessionId)
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]

        mVisualizer!!.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {
                mVisualizerView!!.updateVisualizer(bytes)
            }

            override fun onFftDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {}
        }, Visualizer.getMaxCaptureRate() / 2, true, false)
    }

    override fun onPause() {
        super.onPause()
    }

    private fun releasemVisualizermEqualizer() {
        if (equlizerstart) {
            mVisualizer!!.release()
            mEqualizer!!.release()
        }
    }

    fun autoscroll() {
        if (scrollview == null) return
        scrollview!!.post { scrollview!!.fullScroll(View.FOCUS_DOWN) }
    }

    fun BUClick(view: View) {
        checkRating()
    }

    fun runAdAgain(chick: Boolean?) {
        SettingSaved.isfullscreenadshow = chick
        val settingSaved = SettingSaved(applicationContext)
        settingSaved.SaveData()
        settingSaved.LoadData()
    }

    /*public class BackgroundAudioService extends Service {


         SimpleExoPlayer player;
        Context context;


        @Override
        public void onCreate() {
            super.onCreate();
            context = getApplicationContext();
            player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context, "My_channel_id", R.string.app_name, R.string.app_name, 123, mediaDescriptionAdapter, new PlayerNotificationManager.NotificationListener() {
                @Override
                public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                    stopSelf();
                }

                @Override
                public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    if(ongoing) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                        } else {
                            startForeground(notificationId, notification);
                        }
                    }
                }
            });
            player.addListener(new Player.EventListener() {

                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity(int reason) {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                }

                @Override
                public void onSeekProcessed() {

                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    int pos = player.getCurrentWindowIndex();
                    PlayerSingleton.getInstance().audioFile = PlayerSingleton.getInstance().playingList.get(pos);
                *//*Bitmap b = coverpicture(PlayerSingleton.getInstance().audioFile.getPath());
                if(b==null){
                    playerView.setBackground();
                }else{
                    playerView.setBackground(null);
                    playerView.setBackgroundColor(Color.parseColor("#FF121212"));
                }*//*
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if(!isPlaying){
                        stopForeground(false);
                    }else{
                        playerNotificationManager.setPlayer(player);
                        player.play();
                    }
                }
            });
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            playerNotificationManager.setPlayer(null);
            player.release();
            player = null;
            PlayerSingleton.getInstance().audioFile = null;
            PlayerSingleton.getInstance().playingList = new ArrayList<>();
            new Thread(()->{
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                if (appProcesses != null){
                    final String packageName = context.getPackageName();
                    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                            Intent i = new Intent(this, PlayerActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("STOP", "STOP");
                            context.startActivity(i);
                        }
                    }
                }
            }).start();


        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public String getCurrentSubText(Player player) {
                return " ";
            }

            @Override
            public String getCurrentContentTitle(Player player) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getTitle();
                else
                    return "";
            }

            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                Intent intentForeground = new Intent(context, PlayerActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return PendingIntent.getActivity(getApplicationContext(), 0, intentForeground, PendingIntent.FLAG_IMMUTABLE);
            }

            @Override
            public String getCurrentContentText(Player player) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getAlbum();
                else
                    return "";
            }

            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return coverpicture(PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getPath());
                else
                    return null;
            }
        };


        PlayerNotificationManager playerNotificationManager;
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {


            String where = intent.getStringExtra("WHERE");
            String path = intent.getStringExtra("PATH");

            if(PlayerSingleton.getInstance().audioFile!=null) {
                if (!path.equals(PlayerSingleton.getInstance().audioFile.getPath())) {
                    player.pause();
                }
            }


            new StartOperation(where,path).execute();





            return START_STICKY;
        }

        static Bitmap coverpicture(String path) {
            MediaMetadataRetriever mr;
            byte[] byte1 = new byte[1];

            mr = new MediaMetadataRetriever();
            mr.setDataSource(path);
            try {
                byte1 = mr.getEmbeddedPicture();
                mr.release();
            }catch (Exception e){
                e.printStackTrace();
            }



            if(byte1 != null) {
                return BitmapFactory.decodeByteArray(byte1, 0, byte1.length);
            }
            else {
                return null;
            }

        }


        public class StartOperation extends AsyncTask{

            String where,path;
            int pos;
            ConcatenatingMediaSource concatenatingMediaSource;

            public StartOperation(String where,String path){
                this.where = where;
                this.path = path;
            }

            @Override
            protected Object doInBackground(Object... objects) {

                PlayerSingleton playerSingleton = PlayerSingleton.getInstance();

                playerSingleton.playingList = new ArrayList<>();
                playerSingleton.audioFile = null;

                if(where!=null&&path!=null){
                    switch (where) {
                        case "search":
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().getAudioFilesStatic();
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "fav":
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().getAllFavStatic();
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "folder":
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().selectByFolderStatic(playerSingleton.audioFile.getFolder());
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "album":
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().selectByAlbumStatic(playerSingleton.audioFile.getAlbum());
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        default:
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show());

                            stopSelf();
                    }
                }


                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                        context, Util.getUserAgent(context, getString(R.string.app_name)));

                concatenatingMediaSource = new ConcatenatingMediaSource();
                for (int i = 0; i < playerSingleton.playingList.size(); i++) {
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(playerSingleton.playingList.get(i).getPath()));
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
                    concatenatingMediaSource.addMediaSource(mediaSource);
                }



                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                player.setMediaSource(concatenatingMediaSource);
                player.prepare();
                player.seekTo(pos,0);
                Log.e("TOT"+PlayerSingleton.getInstance().playingList.size(),"pos"+pos);
                playerNotificationManager.setPlayer(player);
                playerView.setPlayer(player);
                playerControlView.setPlayer(player);
                player.play();
            }
        }

    }*/
}
