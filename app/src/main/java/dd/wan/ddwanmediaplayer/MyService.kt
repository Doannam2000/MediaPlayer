package dd.wan.ddwanmediaplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast

class MyService : Service() {
    var mediaPlayer = MediaPlayer()
    var list = ArrayList<Podcast>()
    var type = 0
    var position = 0
    var currentTime = 0
    var action = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        list = ReadPodcast(this).loadSong()
        var bundle = intent!!.extras
        action = bundle!!.getInt("action")
        var uri = bundle.getString("Uri")
        for (i in list.indices) {
            if (list[i].uri == uri)
                position = i
        }
        type = bundle.getInt("type")
        currentTime = bundle.getInt("currentTime")

        when (action) {
            0 -> {
                playSong()
                updateTime()
                createNotification()
            }
            1 -> {
                if (type == 2) {
                    list.shuffle()
                } else {
                    position--
                    if (position < 0) {
                        position = list.size - 1
                    }
                }
                currentTime = 0
                playSong()
                updateTime()
                createNotification()
                sendDataToActivity()
            }
            2 -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                }
                createNotification()
            }
            3 -> {
                if (type == 2) {
                    list.shuffle()
                } else {
                    position++
                    if (position == list.size) {
                        position = 0
                    }
                }
                currentTime = 0
                playSong()
                updateTime()
                createNotification()
                sendDataToActivity()
            }
            4 -> {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }


    @SuppressLint("RemoteViewLayout")
    fun createNotification() {
        var intent = Intent(this, PlayActivity::class.java)
        var bundle = Bundle()
        bundle.putString("Uri", list[position].uri)
        bundle.putInt("type", type)
        bundle.putInt("action", action)
        bundle.putInt("currentTime", mediaPlayer.currentPosition)
        intent.putExtras(bundle)
        var pendingIntent =
            PendingIntent.getActivity(
                this,
                123,
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val podcast = list[position]

        var remoteView = RemoteViews(packageName, R.layout.custom_notification)
        remoteView.setTextViewText(R.id.name, podcast.title)
        remoteView.setTextViewText(R.id.name2, podcast.artist)

        if (mediaPlayer.isPlaying) {
            remoteView.setImageViewResource(R.id.btnPlayN, R.drawable.ic_baseline_pause_24)
        } else {
            remoteView.setImageViewResource(R.id.btnPlayN, R.drawable.ic_outline_play_arrow_24)
        }
        remoteView.setOnClickPendingIntent(R.id.btnNextN, sendAction(3))
        remoteView.setOnClickPendingIntent(R.id.btnPrevious, sendAction(1))
        remoteView.setOnClickPendingIntent(R.id.btnPlayN, sendAction(2))
        remoteView.setOnClickPendingIntent(R.id.btnExit, sendAction(4))

        if (podcast.image.isNotEmpty())
            remoteView.setImageViewBitmap(
                R.id.imageView,
                BitmapFactory.decodeByteArray(podcast.image, 0, podcast.image!!.size)
            )
        var notification = NotificationCompat.Builder(this, "DDWAN")
            .setSmallIcon(R.drawable.music_icon)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteView)
            .build()
        startForeground(123, notification)
    }

    private fun sendAction(ac: Int): PendingIntent? {
        var intent = Intent(this, Broadcast::class.java)
        var bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putInt("type", type)
        bundle.putInt("action", ac)
        bundle.putInt("currentTime", 0)
        intent.putExtras(bundle)
        return PendingIntent.getBroadcast(
            this.applicationContext,
            ac,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }

    fun playSong() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(list[position].uri)
        mediaPlayer.prepare()
        mediaPlayer.seekTo(currentTime)
        mediaPlayer.start()
    }

    fun updateTime() {
        var handle = Handler()
        handle.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer.setOnCompletionListener {
                    currentTime = 0
                    when (type) {
                        0 -> {
                            position++
                            if (position == list.size) {
                                position = 0
                            }
                            playSong()
                            sendDataToActivity()
                        }
                        1 -> {
                            playSong()
                        }
                        2 -> {
                            list.shuffle()
                            playSong()
                            sendDataToActivity()
                        }
                        3 -> {
                            position++
                            if (position == list.size)
                                mediaPlayer.stop()
                            else {
                                playSong()
                                sendDataToActivity()
                            }

                        }
                    }
                }
                sendCurrentPosition()
                handle.postDelayed(this, 500)
            }
        }, 100)
    }

    fun sendCurrentPosition() {
        var intent = Intent("Current_Position")
        var bundle = Bundle()
        bundle.putInt("currentPosition", mediaPlayer.currentPosition)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun sendDataToActivity() {
        var intent = Intent("Current_Song")
        var bundle = Bundle()
        bundle.putString("Uri", list[position].uri)
        bundle.putInt("type", type)
        bundle.putInt("action", action)
        bundle.putInt("currentTime", 0)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}