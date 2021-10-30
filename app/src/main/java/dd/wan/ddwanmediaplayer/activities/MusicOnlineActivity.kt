package dd.wan.ddwanmediaplayer.activities

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.connectService
import dd.wan.ddwanmediaplayer.config.Constants.Companion.getCurrentSong
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import dd.wan.ddwanmediaplayer.config.Constants.Companion.song
import dd.wan.ddwanmediaplayer.config.Constants.Companion.activity
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isFavorite
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isMyServiceRunning
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.config.Constants.Companion.uri
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import dd.wan.ddwanmediaplayer.viewmodel.MyViewModel
import kotlinx.android.synthetic.main.activity_music_online.*
import kotlinx.android.synthetic.main.activity_music_online.btnNextN
import kotlinx.android.synthetic.main.activity_music_online.btnPlayN
import kotlinx.android.synthetic.main.activity_music_online.btnPrevious
import kotlinx.android.synthetic.main.activity_music_online.imageP
import kotlinx.android.synthetic.main.activity_music_online.nameAuth
import kotlinx.android.synthetic.main.activity_music_online.nameSong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class MusicOnlineActivity : AppCompatActivity(), DataTransmission {

    private var exit = 0
    val model by lazy {
        ViewModelProvider(this).get(MyViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        exit = 0
        if (getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).getString("Uri", "") != "")
            layout_music_play.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_online)
        supportActionBar?.hide()

        var navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        var controller = navHostFragment.navController
        bottomNavigationView.setupWithNavController(controller)
        layout_music_play.visibility = View.VISIBLE

        model.listMusic.observe(this, Observer { data ->
            listRecommendMusic.clear()
            listRecommendMusic.add(song)
            listRecommendMusic.addAll(data)
        })

        if (getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).getString("Uri", "") == "") {
            layout_music_play.visibility = View.GONE
        } else {
            getCurrentSong(this)
            if (online) {
                model.getRecommendMusic()
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
            backToPlayActivity()
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))

    }


    private fun backToPlayActivity() {
        activity = if (activity) {
            finish()
            overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
            false
        } else {
            getCurrentSong(this)
            if (online) {
                if (!isMyServiceRunning(MyService::class.java, this)) {
                    model.getRecommendMusic()
                    startActivityOrService(startAc = true, startSer = true, ac = ACTION_CHANGE)
                } else {
                    model.getRecommendMusic()
                    startActivityOrService(startAc = true, startSer = false, ac = ACTION_CHANGE)
                }
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

    private fun startActivityOrService(startAc: Boolean, startSer: Boolean, ac: Int) {
        val bundle = Bundle()
        bundle.putInt("action", ac)
        if (startAc) {
            val intent1 = Intent(this, PlayActivity::class.java)
            intent1.putExtras(bundle)
            val options = ActivityOptions.makeCustomAnimation(this,
                R.anim.right_to_left,
                R.anim.right_to_left_out)
            startActivity(intent1, options.toBundle())
            activity = false
            finish()
        }
        if (startSer) {
            val intent = Intent(this, MyService::class.java)
            intent.putExtras(bundle)
            startService(intent)
        }
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
        position = position1
        if (online)
            Constants.song = song
        if (isFavorite1 == 1) {
            isFavorite = true
//            listRecommendMusic.clear()
//            listFavorite.forEach {
//                listRecommendMusic.add(Constants.getFavoriteSong(it))
//            }
            if (Constants.isNetworkConnected(this) && isFavorite || !isFavorite)
                startActivityOrService(startAc = true,
                    startSer = true,
                    ac = MyApplication.ACTION_PLAY_SONG)
            else
                Toast.makeText(this,"Không thể kết nối internet !!!",Toast.LENGTH_SHORT).show()
        } else {
            if (online) {
                isFavorite = false
                uri = song.id
                model.getRecommendMusic()
                if (Constants.isNetworkConnected(this))
                    GlobalScope.launch {
                        delay(100)
                        startActivityOrService(startAc = true,
                            startSer = true,
                            ac = MyApplication.ACTION_PLAY_SONG)
                    }
                else
                    Toast.makeText(this,"Không thể kết nối internet !!!",Toast.LENGTH_SHORT).show()
            } else {
                startActivityOrService(startAc = true,
                    startSer = true,
                    ac = MyApplication.ACTION_PLAY_SONG)
            }
        }
        layout_music_play.visibility = View.VISIBLE
        updateUI()

    }

    override fun onBackPressed() {
        exit++
        if (exit == 1)
            Toast.makeText(applicationContext, "Nhấn back thêm 1 lần để thoát", Toast.LENGTH_SHORT)
                .show()
        else {
            var homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
        }
    }
}