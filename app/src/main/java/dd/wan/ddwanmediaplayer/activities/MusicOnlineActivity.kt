package dd.wan.ddwanmediaplayer.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.connectService
import dd.wan.ddwanmediaplayer.config.Constants.Companion.getCurrentSong
import dd.wan.ddwanmediaplayer.config.Constants.Companion.getRecommendSong
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import dd.wan.ddwanmediaplayer.config.Constants.Companion.song
import dd.wan.ddwanmediaplayer.config.Constants.Companion.activity
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isFavorite
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isMyServiceRunning
import dd.wan.ddwanmediaplayer.config.Constants.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.activity_music_online.*
import kotlinx.android.synthetic.main.activity_music_online.btnExit
import kotlinx.android.synthetic.main.activity_music_online.btnNextN
import kotlinx.android.synthetic.main.activity_music_online.btnPlayN
import kotlinx.android.synthetic.main.activity_music_online.btnPrevious
import kotlinx.android.synthetic.main.activity_music_online.imageP
import kotlinx.android.synthetic.main.activity_music_online.nameAuth
import kotlinx.android.synthetic.main.activity_music_online.nameSong
import kotlinx.android.synthetic.main.activity_play.*
import java.lang.Exception

class MusicOnlineActivity : AppCompatActivity(), DataTransmission {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_online)

        var navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        var controller = navHostFragment.navController
        bottomNavigationView.setupWithNavController(controller)

        layout_music_play.visibility = View.VISIBLE

        if (getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).getString("Uri",
                "") == ""
        ) {
            layout_music_play.visibility = View.GONE
        } else {
            getCurrentSong(this)
            if (online) {
                getRecommendSong(false, startSer = false, ACTION_PLAY_SONG, this)
            }
        }
        updateUI()

        btnExit.setOnClickListener {
            connectService(MyApplication.ACTION_STOP_SONG, this)
            layout_music_play.visibility = View.GONE
        }
        btnNextN.setOnClickListener { connectService(MyApplication.ACTION_NEXT_SONG, this) }
        btnPrevious.setOnClickListener {
            connectService(MyApplication.ACTION_PREVIOUS_SONG, this)
        }
        btnPlayN.setOnClickListener {
            connectService(MyApplication.ACTION_PAUSE_OR_PLAY, this)
        }

        if (isMyServiceRunning(MyService::class.java, this)) {
            connectService(MyApplication.ACTION_CHECK, this)
        }

        layout_music_play.setOnClickListener {
            activity = if (activity) {
                finish()
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                false
            } else {
                getCurrentSong(this)
                if (online) {
                    if (!isMyServiceRunning(MyService::class.java, this))
                        getRecommendSong(true, startSer = true, ACTION_CHANGE, this)
                    else
                        getRecommendSong(true, startSer = false, ACTION_CHANGE, this)
                } else {
                    val intent = Intent(this, PlayActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                    if (!isMyServiceRunning(MyService::class.java, this)) {
                        val bundle = Bundle()
                        bundle.putInt("action", ACTION_CHANGE)
                        val intent2 = Intent(this, MyService::class.java)
                        intent2.putExtras(bundle)
                        startService(intent2)
                    }
                }
            true
        }
    }

    LocalBroadcastManager.getInstance(this)
    .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
    LocalBroadcastManager.getInstance(this)
    .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))

}


private fun updateUI() {
    if ((isFavorite && listFavorite[position].isOnline) || (online && !isFavorite)) {
        nameSong.text = song.name
        nameAuth.text = song.artists_names
        Glide.with(applicationContext).load(song.thumbnail).into(imageP)
        if (check)
            btnPlayN.setImageResource(R.drawable.ic_baseline_pause_24)
        else
            btnPlayN.setImageResource(R.drawable.ic_outline_play_arrow_24)
    } else {
        val podcast = if (isFavorite) {
            listFavorite[position].song
        } else {
            list[position]
        }
        nameSong.text = podcast.title
        nameAuth.text = podcast.artist
        if (podcast.image.isNotEmpty()) {
            try {
                val image = podcast.image
                imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
            } catch (e: Exception) {
                imageP.setImageResource(R.drawable.music_icon)
            }
        } else {
            imageP.setImageResource(R.drawable.music_icon)
        }
    }

}

private val broadcastPlay = object : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        check = p1!!.extras!!.getBoolean("checked")
        if (check)
            btnPlayN.setImageResource(R.drawable.ic_baseline_pause_24)
        else
            btnPlayN.setImageResource(R.drawable.ic_outline_play_arrow_24)
    }
}

private val broadcastPodcast = object : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val bundle = p1?.extras
        if (bundle == null)
            return
        else {
            updateUI()
        }
    }
}


override fun onDestroy() {
    super.onDestroy()
    LocalBroadcastManager.getInstance(this)
        .unregisterReceiver(broadcastPodcast)
    LocalBroadcastManager.getInstance(this)
        .unregisterReceiver(broadcastPlay)
}

override fun ChangeData(
    check1: Boolean,
    online1: Boolean,
    activity1: Boolean,
    currentTime1: Int,
    position1: Int,
    isFavorite1: Int,
    song: Song,
) {
    check = check1
    online = online1
    activity = activity1
    currentTime = currentTime1
    if (isFavorite1 == 1) {
        isFavorite = true
        listRecommendMusic.clear()
        listFavorite.forEach {
            listRecommendMusic.add(Constants.getFavoriteSong(it))
        }
    } else
        isFavorite = false

    position = position1
    if (online)
        Constants.song = song
    updateUI()
}

override fun onBackPressed() {
    finish()
}
}