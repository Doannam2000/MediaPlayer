package dd.wan.ddwanmediaplayer.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHECK
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NEXT_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NOT_REPEAT
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PAUSE_OR_PLAY
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PREVIOUS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_ALL
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_THIS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_STOP_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_TIMER
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.timer
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.config.Constants.Companion.uri
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.config.Constants.Companion.song
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class MyService : Service() {

    var mediaPlayer = MediaPlayer()
    var type = 0
    var action = 0
    var shuffle = false
    var isStop = false
    var checkTimer = false
    val arrayPlayed = mutableSetOf<Int>()

    lateinit var countDown: CountDownTimer
    private val handleTime = Handler()
    private val runTime = Runnable {
        val duration = TimeUnit.MINUTES.toMillis(timer.toLong())
        countDown = object : CountDownTimer(duration, 1000) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                timer = 0
                val sharedPreferences =
                    getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
                sharedPreferences.edit().putInt("timer", timer).apply()
                currentTime = 0
                checkTimer = false
                mediaPlayer.pause()
                playOrPause(false, exit = false)
                createNotification()
            }
        }
        countDown.start()
    }

    var myBinder = MyBinder()

    inner class MyBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return myBinder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent!!.extras
        action = bundle!!.getInt("action")
        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        type = sharedPreferences.getInt("type", 0)
        timer = sharedPreferences.getInt("timer", 0)
        shuffle = sharedPreferences.getBoolean("shuffle", false)
        checkTimer = timer != 0
        if (shuffle)
            arrayPlayed.add(position)
        when (action) {
            ACTION_PLAY_SONG -> {
                play()
            }
            ACTION_PREVIOUS_SONG -> {
                clearArrayPlayed()
                previous()
            }
            ACTION_PAUSE_OR_PLAY -> {
                playOr()
            }
            ACTION_NEXT_SONG -> {
                clearArrayPlayed()
                nextSong()
            }
            ACTION_STOP_SONG -> {
                playOrPause(check = false, exit = true)
                stopSelf()
            }
            ACTION_TIMER -> {
                if (checkTimer) {
                    if (this::countDown.isInitialized)
                        countDown.cancel()
                    handleTime.postDelayed(runTime, 100)
                } else {
                    if (this::countDown.isInitialized)
                        countDown.cancel()
                    handleTime.removeCallbacks(runTime)
                }
                GlobalScope.launch {
                    createNotification()
                }
            }
            ACTION_CHECK -> {
                if (mediaPlayer.isPlaying)
                    playOrPause(check = true, exit = false)
                else
                    playOrPause(check = false, exit = false)
            }
            ACTION_CHANGE -> {
                setUpSong()
                playOrPause(false, exit = false)
            }
        }

        return START_NOT_STICKY
    }

    fun play() {
        playSong()
        createNotification()
        playOrPause(true, exit = false)
    }

    fun previous() {
        if (shuffle) {
            randomSong()
        } else {
            position--
            if (position < 0) {
                position = if (online) listRecommendMusic.size - 1 else list.size - 1
            }
        }
        currentTime = 0
        playSong()
        createNotification()
        sendDataToActivity()
    }

    fun playOr() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playOrPause(false, exit = false)
        } else {
            if (isStop)
                playSong()
            else
                mediaPlayer.start()
            playOrPause(true, exit = false)
        }
        createNotification()
    }

    fun nextSong() {
        if (shuffle) {
            randomSong()
        } else {
            position++
            if (online && position == listRecommendMusic.size) {
                position = 0
            }
            if (!online && position == list.size) {
                position = 0
            }
        }
        currentTime = 0
        playSong()
        createNotification()
        sendDataToActivity()
    }


    @SuppressLint("RemoteViewLayout")

    fun createNotification() {
        val intent = Intent(this, PlayActivity::class.java)
        check = mediaPlayer.isPlaying
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                123,
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val remoteView = RemoteViews(packageName, R.layout.custom_notification)
        val notification = NotificationCompat.Builder(this, "DDWAN")
            .setSmallIcon(R.drawable.music_icon)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteView)
            .build()
        if (!online) {
            val podcast = list[position]
            remoteView.setTextViewText(R.id.name, podcast.title)
            remoteView.setTextViewText(R.id.name2, podcast.artist)
            if (podcast.image.isNotEmpty()) {
                try {
                    val image = list[position].image
                    remoteView.setImageViewBitmap(R.id.imageView,
                        BitmapFactory.decodeByteArray(image, 0, image.size))
                } catch (e: Exception) {
                    remoteView.setImageViewResource(R.id.imageView, R.drawable.music_icon)
                }
            } else {
                remoteView.setImageViewResource(R.id.imageView, R.drawable.music_icon)
            }
        } else {
            val podcast = listRecommendMusic[position]
            remoteView.setTextViewText(R.id.name, podcast.name)
            remoteView.setTextViewText(R.id.name2, podcast.artists_names)
            val target = NotificationTarget(applicationContext,
                R.id.imageView,
                remoteView,
                notification,
                123)
            GlobalScope.launch {
                Glide.with(baseContext)
                    .asBitmap()
                    .load(podcast.thumbnail)
                    .into(target)
            }
        }
        if (mediaPlayer.isPlaying) {
            remoteView.setImageViewResource(R.id.btnPlayN, R.drawable.ic_baseline_pause_24)
        } else {
            remoteView.setImageViewResource(R.id.btnPlayN, R.drawable.ic_outline_play_arrow_24)
        }
        remoteView.setOnClickPendingIntent(R.id.btnNextN, sendAction(ACTION_NEXT_SONG))
        remoteView.setOnClickPendingIntent(R.id.btnPrevious, sendAction(ACTION_PREVIOUS_SONG))
        remoteView.setOnClickPendingIntent(R.id.btnPlayN, sendAction(ACTION_PAUSE_OR_PLAY))
        remoteView.setOnClickPendingIntent(R.id.btnExit, sendAction(ACTION_STOP_SONG))
        startForeground(123, notification)
    }

    private fun setUpSong() {
        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer()
        var url = if (online) {
            edit.putString("nameSong", listRecommendMusic[position].name)
            edit.putString("artists_names", listRecommendMusic[position].artists_names)
            edit.putString("thumbnail", listRecommendMusic[position].thumbnail)
            edit.putInt("duration", listRecommendMusic[position].duration)
            edit.putString("Uri", listRecommendMusic[position].id)
            edit.apply()
            song = listRecommendMusic[position]
            "https://api.mp3.zing.vn/api/streaming/audio/${listRecommendMusic[position].id}/320"
        } else {
            sharedPreferences.edit().putString("Uri", list[position].uri).apply()
            list[position].uri
        }
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepare()
        mediaPlayer.seekTo(currentTime)
        mediaPlayer.setOnCompletionListener {
            currentTime = 0
            when (type) {
                ACTION_REPEAT_ALL -> {
                    clearArrayPlayed()
                    nextSong()
                }
                ACTION_REPEAT_THIS_SONG -> {
                    playSong()
                }
                ACTION_NOT_REPEAT -> {
                    if (shuffle) {
                        if ((arrayPlayed.size < list.size && !online) || (online && arrayPlayed.size < listRecommendMusic.size))
                            nextSong()
                        else {
                            stopSong()
                        }
                    } else {
                        position++
                        if ((position == list.size && !online) || (online && arrayPlayed.size < listRecommendMusic.size)) {
                            stopSong()
                        } else {
                            playSong()
                            GlobalScope.launch {
                                createNotification()
                            }
                            sendDataToActivity()
                        }
                    }
                }
            }
        }
    }

    private fun playSong() {
        setUpSong()
        mediaPlayer.start()
    }

    private fun sendDataToActivity() {
        val intent = Intent("Current_Song")
        val bundle = Bundle()
        if (online)
            bundle.putSerializable("Song", listRecommendMusic[position])
        else
            bundle.putString("Uri", list[position].uri)
        bundle.putInt("action", action)
        bundle.putBoolean("online", online)
        bundle.putInt("currentTime", currentTime)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        playOrPause(true, exit = false)
    }

    private fun sendAction(ac: Int): PendingIntent? {
        val intent = Intent(this, Broadcast::class.java)
        val bundle = Bundle()
        bundle.putInt("type", type)
        bundle.putInt("action", ac)
        intent.putExtras(bundle)
        return PendingIntent.getBroadcast(
            this.applicationContext,
            ac,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }


    fun playOrPause(check: Boolean, exit: Boolean) {
        val intent = Intent("Pause_Play")
        val bundle = Bundle()
        bundle.putBoolean("checked", check)
        bundle.putBoolean("exit", exit)
        bundle.putInt("timer", timer)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun randomSong() {
        val random = Random()
        if (online) {
            position = random.nextInt(listRecommendMusic.size)
            while (arrayPlayed.contains(position)) {
                position = random.nextInt(listRecommendMusic.size)
            }
            arrayPlayed.add(position)
        } else {
            position = random.nextInt(list.size)
            while (arrayPlayed.contains(position)) {
                position = random.nextInt(list.size)
            }
            arrayPlayed.add(position)
        }
    }

    private fun stopSong() {
        position = 0
        mediaPlayer.seekTo(0)
        isStop = true
        mediaPlayer.stop()
        GlobalScope.launch {
            createNotification()
        }
        sendDataToActivity()
        playOrPause(false, exit = false)
        arrayPlayed.clear()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        check = false
        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("timer", 0).apply()
        super.onDestroy()
    }

    private fun clearArrayPlayed() {
        if (online) {
            if (arrayPlayed.size == listRecommendMusic.size) {
                arrayPlayed.clear()
            }
        } else {
            if (arrayPlayed.size == list.size) {
                arrayPlayed.clear()
            }
        }
    }
}


