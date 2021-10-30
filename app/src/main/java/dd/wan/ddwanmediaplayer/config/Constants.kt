package dd.wan.ddwanmediaplayer.config

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.service.MyService

import androidx.appcompat.app.AppCompatActivity
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.Broadcast
import java.text.SimpleDateFormat

import android.net.ConnectivityManager
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listRecommendMusic
import java.lang.Exception


class Constants {
    companion object {
        const val BASE_URL = "https://mp3.zing.vn/"
        const val BASE_URL_SEARCH = "http://ac.mp3.zing.vn/"
        var currentTime = 0
        var check = false
        var timer = 0
        var activity = false
        var online = true
        var position = 0
        var isFavorite = false
        var song = Song()
        var uri = ""
        var listSong = ArrayList<Song>()

        @SuppressLint("SimpleDateFormat")
        val sdf = SimpleDateFormat("mm:ss")


        fun getCurrentSong(context: Context) {
            val shared = context.getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
            uri = shared.getString("Uri", "").toString()
            if (uri != "" && uri!!.contains("/")) {
                online = false
                for (i in MyApplication.list.indices) {
                    if (MyApplication.list[i].uri == uri)
                        position = i
                }
            } else if (uri != "" && !uri.contains("/")) {
                online = true
                song.id = uri
                song.name = shared.getString("nameSong", "Tên bài hát")!!
                song.artists_names = shared.getString("artists_names", "Tên ca sĩ")!!
                song.thumbnail = shared.getString("thumbnail", "")!!
                song.duration = shared.getInt("duration", 0)
                listRecommendMusic.clear()
                listRecommendMusic.add(song)
            }
        }

        fun connectService(ac: Int, context: Context) {
            val bundle = Bundle()
            if (isMyServiceRunning(MyService::class.java, context)) {
                bundle.putInt("action", ac)
                val intent = Intent(context, Broadcast::class.java)
                intent.putExtras(bundle)
                context.sendBroadcast(intent)
            } else {
                if (ac == MyApplication.ACTION_PAUSE_OR_PLAY)
                    bundle.putInt("action", MyApplication.ACTION_PLAY_SONG)
                else
                    bundle.putInt("action", ac)
                val intent = Intent(context, MyService::class.java)
                intent.putExtras(bundle)
                context.startService(intent)
            }
        }

        fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            val manager =
                context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun getFavoriteSong(favoriteMusic: FavoriteSong): Song {
            val song1 = Song()
            song1.name = favoriteMusic.song.title
            song1.id = favoriteMusic.song.uri
            song1.artists_names = favoriteMusic.song.artist
            song1.thumbnail = favoriteMusic.thumbnail
            song1.duration = favoriteMusic.song.duration
            return song1
        }


        fun isNetworkConnected(context: Context): Boolean {
            return try {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                 cm!!.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
            }catch (e:Exception){
                false
            }
        }
    }
}