package com.medoapps.www.onlinequran

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.medoapps.www.onlinequran.util.Permissions
import com.medoapps.www.onlinequran.util.SeparateFunctions
import java.util.Random

/**
 * Created by MEDO on 3/02/2022.
 */
class MediaPlayerService : Service(), Player.Listener,
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val ACTION_PLAY = "com.medoapps.www.onlinequran.ACTION_PLAY"
        const val ACTION_PAUSE = "com.medoapps.www.onlinequran.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.medoapps.www.onlinequran.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.medoapps.www.onlinequran.ACTION_NEXT"
        const val ACTION_STOP = "com.medoapps.www.onlinequran.ACTION_STOP"
        const val ACTION_CLOSE = "com.medoapps.www.onlinequran.ACTION_CLOSE"
        private const val NOTIFICATION_ID = 101
        private const val WAKE_LOCK = 333
    }

    private var exoPlayer: ExoPlayer? = null

    // MediaSession
    private var mediaSessionManager: androidx.media.MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    // Used to pause/resume MediaPlayer
    private var resumePosition = 0L

    // AudioFocus
    private lateinit var audioManager: AudioManager

    // Binder given to clients
    private val iBinder: IBinder = LocalBinder()

    // List of available Audio files
    private var audioList: ArrayList<Audio>? = null
    private var audioIndex = -1
    private var activeAudio: Audio? = null

    // Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null
    private lateinit var telephonyManager: TelephonyManager
    private var newCallStateListener: TelephonyCallback.CallStateListener? = null
    var playbackStatusPublic: PlaybackStatus? = null

    private var nManager: NotificationManager? = null
    private var CHANNEL_ID = "HASHIM_CHANNEL-ID_02"
    private var nBuilder: NotificationCompat.Builder? = null
    private var notification: Notification? = null
    private var canContiueAfterFucus = false
    private var wifiLock: android.net.wifi.WifiManager.WifiLock? = null

    private var pm: PowerManager? = null
    private var wl: PowerManager.WakeLock? = null

    private val TAG = "MediaPlayerService"

    var instance: MediaPlayerService? = null

    /**
     * Service lifecycle methods
     */
    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun startWakeLock() {
        pm = applicationContext.getSystemService(PowerManager::class.java)
        wl = pm!!.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK.toString()
        )
        if (!wl!!.isHeld) {
            wl!!.acquire()
        }
    }

    private fun closeWakeLock() {
        if (wl != null) wl!!.release()
        wl = null
        pm = null
    }

    private fun batteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        callStateListener()
        registerBecomingNoisyReceiver()
        register_playNewAudio()
        Log.d(TAG, "onCreate: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            instance = this
            Log.d(TAG, "onStartCommand: ")
            val storage = StorageUtil(applicationContext)
            audioList = storage.loadAudio()
            audioIndex = storage.loadAudioIndex()

            if (audioIndex != -1 && audioIndex < audioList!!.size) {
                activeAudio = audioList!![audioIndex]
            } else {
                stopSelf()
            }
        } catch (e: NullPointerException) {
            stopSelf()
        }

        if (!requestAudioFocus()) {
            stopSelf()
        }

        if (mediaSessionManager == null && mediaSession == null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startWakeLock()
                }
                Log.d(TAG, "onStartCommand:  test")
                initMediaSession()
                initMediaPlayer()
                updateMetaData()
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        handleIncomingActions(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    /*override fun onUnbind(intent: Intent): Boolean {
        mediaSession.release()
        return super.onUnbind(intent)
    }*/

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroysasd: onDestroy")
        destroyService()
        instance = null
    }

    private fun destroyService() {
        val storage = StorageUtil(applicationContext)
        if (exoPlayer != null) {
            pauseMedia()
            stopMedia()
            exoPlayer!!.release()
            exoPlayer = null
        }
        removeAudioFocus()
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        storage.storeIsMediaStoppedFromUser(false)
        removeNotification()
        closeWakeLock()
        stopSelf()
    }

    /**
     * Service Binder
     */
    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService

        fun destroyFromOutside() {
            destroyService()
        }
    }

    /**
     * Player.Listener callbacks (replaces MediaPlayer listeners)
     */
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                Log.d(TAG, "onPlaybackStateChanged: STATE_READY")
                val storage = StorageUtil(applicationContext)
                Log.d(TAG, "STATE_READY isMediaStoppedFromUser: ${storage.loadIsMediaStoppedFromUser()}")

                // Always notify the UI so the loading spinner is dismissed
                val broadcastIntent = Intent(NewQuranPlayer.Broadcast_updateProgressBarReceiver)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)

                if (!storage.loadIsMediaStoppedFromUser()) {
                    playMedia()
                    setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 347)
                }
                val permissions = Permissions(this, null)

                if (!activeAudio!!.data.contains("http")) {
                    if (!permissions.checkStoragePermissionForService()) {
                        Log.d(TAG, "STATE_READY: error load, no permission to open audio")
                        skipToNext()
                    }
                }
            }
            Player.STATE_ENDED -> {
                Log.d(TAG, "onPlaybackStateChanged: STATE_ENDED")
                val storage = StorageUtil(applicationContext)

                when {
                    storage.loadIsPlayerRepeat() -> repeatMedia()
                    storage.loadIsPlayerShuffle() -> shuffleMedia()
                    else -> SkipToNextPublic()
                }
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.d(TAG, "onPlayerError: ${error.errorCode} - ${error.message}")
    }

    override fun onAudioFocusChange(focusState: Int) {
        Log.d(TAG, "onAudioFocusChange: $focusState")
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (canContiueAfterFucus) {
                    if (exoPlayer == null) initMediaPlayer()
                    else if (!exoPlayer!!.isPlaying) exoPlayer!!.play()
                    exoPlayer!!.volume = 1.0f
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (exoPlayer != null && exoPlayer!!.isPlaying) {
                    PausePublic()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (exoPlayer != null && exoPlayer!!.isPlaying) {
                    canContiueAfterFucus = true
                    exoPlayer!!.pause()
                } else {
                    canContiueAfterFucus = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (exoPlayer != null && exoPlayer!!.isPlaying) exoPlayer!!.volume = 0.1f
            }
        }
    }

    /**
     * AudioFocus
     */
    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return try {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    /**
     * MediaPlayer actions
     */
    private fun initMediaPlayer() {
        startMediaSeparatly()
    }

    fun startMediaSeparatly() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
            exoPlayer!!.addListener(this)
            exoPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(), false  // false = we manage audio focus manually
            )
        }

        exoPlayer!!.stop()
        exoPlayer!!.clearMediaItems()

        val dataSource = activeAudio!!.data
        exoPlayer!!.setMediaItem(MediaItem.fromUri(Uri.parse(dataSource)))

        val test = SeparateFunctions(applicationContext)
        Log.d(TAG, "startMediaSeparatly: network=${test.isNetworkAvailable}")
        if (!test.isNetworkAvailable && activeAudio!!.data.contains("http")) {
            Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
            destroyService()
            return
        }

        if (!activeAudio!!.data.contains("http") || test.isNetworkAvailable) {
            exoPlayer!!.prepare()  // always async in ExoPlayer — no race condition
        }
    }

    private fun playMedia() {
        Log.d(TAG, "playMedia: ")
        if (exoPlayer != null && !exoPlayer!!.isPlaying) {
            exoPlayer!!.play()
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 479)
        }
    }

    private fun stopMedia() {
        if (exoPlayer == null) return
        if (exoPlayer!!.isPlaying) {
            exoPlayer!!.stop()
            resumePosition = 0L
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 491)
        }
    }

    private fun pauseMedia() {
        val storage = StorageUtil(applicationContext)
        storage.storeIsMediaStoppedFromUser(true)
        if (exoPlayer != null && exoPlayer!!.isPlaying) {
            resumePosition = exoPlayer!!.currentPosition
            exoPlayer!!.pause()
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED, 500)
        }
    }

    private fun resumeMedia() {
        val storage = StorageUtil(applicationContext)
        storage.storeIsMediaStoppedFromUser(false)
        Log.d(TAG, "resumeMedia: ")
        if (exoPlayer != null && !exoPlayer!!.isPlaying) {
            exoPlayer!!.seekTo(resumePosition)
            exoPlayer!!.play()
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 509)
        }
    }

    private fun skipToNext() {
        val storage = StorageUtil(applicationContext)
        storage.storeIsMediaStoppedFromUser(false)
        if (audioIndex == audioList!!.size - 1) {
            audioIndex = 0
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![++audioIndex]
        }

        StorageUtil(applicationContext).storeAudioIndex(audioIndex)

        stopMedia()
        initMediaPlayer()
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 532)
    }

    private fun repeatMedia() {
        stopMedia()
        initMediaPlayer()
        updateMetaData()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 544)
    }

    private fun shuffleMedia() {
        val rand = Random()
        val currentSongIndex = rand.nextInt(audioList!!.size - 1 - 0 + 1) + 0
        activeAudio = audioList!![currentSongIndex]

        StorageUtil(applicationContext).storeAudioIndex(currentSongIndex)

        stopMedia()
        initMediaPlayer()
        updateMetaData()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 561)
    }

    private fun skipToPrevious() {
        val storage = StorageUtil(applicationContext)
        storage.storeIsMediaStoppedFromUser(false)
        if (audioIndex == 0) {
            audioIndex = audioList!!.size - 1
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![--audioIndex]
        }

        StorageUtil(applicationContext).storeAudioIndex(audioIndex)

        stopMedia()
        initMediaPlayer()
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 585)
    }

    fun STATE_PLAYING_public() {
        updateMetaData()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 592)
    }

    fun SkipToNextPublic() {
        skipToNext()
        updateMetaData()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 600)
    }

    fun SkipToPreviousPublic() {
        skipToPrevious()
        updateMetaData()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 607)
    }

    fun ResumePublic() {
        resumeMedia()
        buildNotification(PlaybackStatus.PLAYING)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 613)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 614)
    }

    fun PausePublic() {
        pauseMedia()
        buildNotification(PlaybackStatus.PAUSED)
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED, 621)
    }

    fun CurrentServiceExoPlayer(): ExoPlayer? {
        return exoPlayer
    }

    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (exoPlayer != null) {
                canContiueAfterFucus = exoPlayer!!.isPlaying
            }
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        ContextCompat.registerReceiver(this, becomingNoisyReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    /**
     * Handle PhoneState changes
     */
    private fun callStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "callStateListener: 31 sdk ")
            telephonyCallback = TeleMan()

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted
            } else {
                telephonyManager.registerTelephonyCallback(mainExecutor, telephonyCallback!!)
            }
        } else {
            phoneStateListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                    Log.d(TAG, "onCallStateChangeddsasd: $state")
                    when (state) {
                        TelephonyManager.CALL_STATE_OFFHOOK,
                        TelephonyManager.CALL_STATE_RINGING -> {
                            if (exoPlayer != null) {
                                canContiueAfterFucus = exoPlayer!!.isPlaying
                                PausePublic()
                                ongoingCall = true
                            }
                        }
                        TelephonyManager.CALL_STATE_IDLE -> {
                            if (exoPlayer != null) {
                                if (ongoingCall) {
                                    ongoingCall = false
                                    if (canContiueAfterFucus) {
                                        resumeMedia()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    /**
     * MediaSession and Notification actions
     */
    @Throws(Exception::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return
        if (mediaSession != null) return

        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        transportControls = mediaSession!!.controller.transportControls
        mediaSession!!.isActive = true
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        updateMetaData()

        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 732)
            }

            override fun onPause() {
                super.onPause()
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED, 741)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 749)
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 753)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, 761)
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 765)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 775)
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
                exoPlayer!!.seekTo(position)
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 785)
            }
        })
    }

    private fun setMediaSessionPlaybackState(state: Int, lineNumber: Int) {
        if (exoPlayer == null) return
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, exoPlayer!!.currentPosition, 1f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()
        )
    }

    fun getTitle(): String {
        return try {
            activeAudio!!.title
        } catch (e: Exception) {
            e.printStackTrace()
            " "
        }
    }

    fun getArtist(): String {
        return try {
            activeAudio!!.artist
        } catch (e: Exception) {
            e.printStackTrace()
            " "
        }
    }

    fun getData(): String {
        return try {
            activeAudio!!.data
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(resources, R.drawable.mystream)

        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio!!.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio!!.album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio!!.title)
                .build()
        )

        if (exoPlayer != null && exoPlayer!!.duration != C.TIME_UNSET) {
            try {
                mediaSession!!.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer!!.duration)
                        .build()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel(parent: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = parent.getString(R.string.channel_name_2)
            val description = parent.getString(R.string.channel_description_2)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.setSound(null, null)
            val notificationManager = parent.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
        Log.d("asdsadsa", "buildNotification: ")
        if (exoPlayer == null) return
        createNotificationChannel(this)
        playbackStatusPublic = playbackStatus
        var notificationAction = android.R.drawable.ic_media_pause
        var play_pauseAction: PendingIntent? = null

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play
            play_pauseAction = playbackAction(0)
        }

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.mystream)

        val controller = mediaSession!!.controller

        nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setShowWhen(false)
            .setColor(resources.getColor(R.color.blue))
            .setLargeIcon(largeIcon)
            .setSmallIcon(getNotificationIcon())
            .setContentText(activeAudio!!.artist)
            .setContentTitle(activeAudio!!.title)
            .setContentInfo(activeAudio!!.album)
            .setContentIntent(createContentIntent())
            .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
            .addAction(notificationAction, "pause", play_pauseAction)
            .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "close", playbackAction(4))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, nBuilder!!.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, nBuilder!!.build())
        }
        Log.d(TAG, "buildNotification: ${activeAudio!!.recitesName}")
    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(applicationContext, NewQuranPlayer::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        Log.d(TAG, "createContentIntent: ${activeAudio!!.recitesName}")
        openUI.putExtra("RecitesName", activeAudio!!.recitesName)
        openUI.putExtra("Rewayat", activeAudio!!.rewayat)
        openUI.putExtra("RealRecitesName", activeAudio!!.realRecitesName)
        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            openUI.putExtra("RecitesAYA", audioIndex.toString())
        } else {
            openUI.putExtra("RecitesAYA", "0")
        }
        openUI.putExtra("IsRadio", activeAudio!!.isRadio)
        openUI.putExtra("isStartFromNotification", true)
        openUI.putExtra("currentPlayerPosition", exoPlayer!!.currentPosition)
        return PendingIntent.getActivity(
            applicationContext, 159, openUI,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                playbackAction.action = ACTION_PLAY
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 1003)
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE)
            }
            1 -> {
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE)
            }
            2 -> {
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE)
            }
            3 -> {
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE)
            }
            4 -> {
                playbackAction.action = ACTION_CLOSE
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE)
            }
            else -> return null
        }
    }

    private fun removeNotification() {
        if (nManager != null) nManager!!.cancel(NOTIFICATION_ID)

        try {
            stopForeground(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return

        val actionString = playbackAction.action
        when {
            actionString.equals(ACTION_PLAY, ignoreCase = true) -> transportControls!!.play()
            actionString.equals(ACTION_PAUSE, ignoreCase = true) -> transportControls!!.pause()
            actionString.equals(ACTION_NEXT, ignoreCase = true) -> transportControls!!.skipToNext()
            actionString.equals(ACTION_PREVIOUS, ignoreCase = true) -> transportControls!!.skipToPrevious()
            actionString.equals(ACTION_STOP, ignoreCase = true) -> transportControls!!.stop()
            actionString.equals(ACTION_CLOSE, ignoreCase = true) -> destroyService()
        }
    }

    /**
     * Play new Audio
     */
    private val playNewAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            audioIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (audioIndex != -1 && audioIndex < audioList!!.size) {
                activeAudio = audioList!![audioIndex]
            } else {
                stopSelf()
            }

            stopMedia()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 1095)
        }
    }

    private fun register_playNewAudio() {
        val filter = IntentFilter(NewQuranPlayer.Broadcast_PLAY_NEW_AUDIO)
        ContextCompat.registerReceiver(this, playNewAudio, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.drawable.mystreamwhite else R.drawable.mystream
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    inner class TeleMan : TelephonyCallback(), TelephonyCallback.ServiceStateListener, TelephonyCallback.CallStateListener {
        override fun onServiceStateChanged(serviceState: ServiceState) {
            Log.d(TAG, "onServiceStateChangedfdfds: $serviceState")
        }

        override fun onCallStateChanged(state: Int) {
            Log.d(TAG, "onCallStateChangedsfdsf: $state")
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK,
                TelephonyManager.CALL_STATE_RINGING -> {
                    if (exoPlayer != null) {
                        canContiueAfterFucus = exoPlayer!!.isPlaying
                        PausePublic()
                        ongoingCall = true
                    }
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (exoPlayer != null && ongoingCall) {
                        ongoingCall = false
                        if (canContiueAfterFucus) {
                            resumeMedia()
                        }
                    }
                }
            }
        }
    }
}
