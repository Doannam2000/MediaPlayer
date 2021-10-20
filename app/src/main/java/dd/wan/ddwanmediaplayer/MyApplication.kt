package dd.wan.ddwanmediaplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.offline.Podcast

class MyApplication : Application() {

    companion object{
        const val ACTION_PLAY_SONG = 0
        const val ACTION_PREVIOUS_SONG = 1
        const val ACTION_PAUSE_OR_PLAY = 2
        const val ACTION_NEXT_SONG = 3
        const val ACTION_STOP_SONG = 4
        const val ACTION_TIMER = 5
        const val ACTION_REPEAT_ALL = 0
        const val ACTION_REPEAT_THIS_SONG = 1
        const val ACTION_NOT_REPEAT = 2
        const val ACTION_CHECK = 6
        const val ACTION_CHANGE = 7
        var list = ArrayList<Podcast>()
        var listFavorite = ArrayList<FavoriteSong>()
    }

    override fun onCreate() {
        super.onCreate()
        createChanelNotification()
    }

    private fun createChanelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "DDWAN",
                "DDWAN_MUSIC",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null,null)
            val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}