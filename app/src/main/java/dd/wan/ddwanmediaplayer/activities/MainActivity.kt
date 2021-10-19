package dd.wan.ddwanmediaplayer.activities

import android.content.BroadcastReceiver
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_STOP_SONG
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.activity_main.*
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.timer
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.activity
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.config.Constants.Companion.connectService
import dd.wan.ddwanmediaplayer.config.Constants.Companion.getCurrentSong
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isMyServiceRunning
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import kotlinx.android.synthetic.main.activity_main.btnExit
import kotlinx.android.synthetic.main.activity_main.btnNextN
import kotlinx.android.synthetic.main.activity_main.btnPlayN
import kotlinx.android.synthetic.main.activity_main.btnPrevious
import kotlinx.android.synthetic.main.activity_main.imageP
import kotlinx.android.synthetic.main.activity_main.nameAuth
import kotlinx.android.synthetic.main.activity_main.nameSong
import kotlinx.android.synthetic.main.activity_main.searchView
import java.lang.Exception

class MainActivity : AppCompatActivity() {



    lateinit var adapter: RecyclerAdapter





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
                currentTime = bundle.getInt("currentTime")
                val uri = bundle.getString("Uri") as String
                for (i in 0 until list.size) {
                    if (list[i].uri == uri)
                        position = i
                }
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val searchView: TextView = findViewById(R.id.searchView)
        val recyclerView: RecyclerView = findViewById(R.id.list_Podcast)



        getCurrentSong(this)

        if (online) {
            nameSong.text = Constants.song.name
            nameAuth.text = Constants.song.artists_names
            Glide.with(applicationContext).load(Constants.song.thumbnail).into(imageP)
        } else {
            nameSong.text = list[position].title
            nameAuth.text = list[position].artist
            if (list[position].image.isNotEmpty()) {
                try {
                    val image = list[position].image
                    imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                } catch (e: Exception) {
                    imageP.setImageResource(R.drawable.music_icon)
                }
            } else {
                imageP.setImageResource(R.drawable.music_icon)
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = RecyclerAdapter(list)
        adapter.setCallback {
            check = true
            online = false
            activity = true
            layoutPodcast.visibility = View.VISIBLE
            val podcast = list[it]
            position = it
            val bundle1 = Bundle()
            bundle1.putString("Uri", podcast.uri)
            bundle1.putInt("currentTime", 0)
            bundle1.putInt("action", ACTION_PLAY_SONG)
            bundle1.putBoolean("online", false)
            bundle1.putBoolean("activity", true)
            bundle1.putInt("timer", timer)
            updateUI()
            val intent11 = Intent(this, MyService::class.java)
            intent11.putExtras(bundle1)
            startService(intent11)

            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtras(bundle1)
            startActivity(intent)
            overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
        }

        recyclerView.adapter = adapter


        btnExit.setOnClickListener {
            connectService(ACTION_STOP_SONG, this)
            layoutPodcast.visibility = View.GONE
        }
        btnNextN.setOnClickListener { connectService(MyApplication.ACTION_NEXT_SONG, this) }
        btnPrevious.setOnClickListener {
            connectService(MyApplication.ACTION_PREVIOUS_SONG,
                this)
        }
        btnPlayN.setOnClickListener {
            connectService(MyApplication.ACTION_PAUSE_OR_PLAY,
                this)
        }

        layoutPodcast.setOnClickListener {
            activity = if (activity) {
                finish()
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                false
            } else {
                getCurrentSong(this)
                if (online) {
                    Constants.getRecommendSong(true, startSer = true, ACTION_CHANGE, this)
                } else {
                    val intent = Intent(this, PlayActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                }
                true
            }
        }

        updateUI()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        if (isMyServiceRunning(MyService::class.java, this)) {
            connectService(MyApplication.ACTION_CHECK, this)
        }
    }

    fun updateUI() {
        nameSong.text = list[position].title
        nameAuth.text = list[position].artist
        if (list[position].image.isNotEmpty()) {
            try {
                val image = list[position].image
                imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
            } catch (e: Exception) {
                imageP.setImageResource(R.drawable.music_icon)
            }
        } else {
            imageP.setImageResource(R.drawable.music_icon)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPlay)
    }

}

//            if (activity) {
//                finish()
//                activity = false
//                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
//            } else {
//                val podcast = list[position]
//                val bundle1 = Bundle()
//                bundle1.putString("Uri", podcast.uri)
//                bundle1.putInt("currentTime", 0)
//                bundle1.putInt("action", currentTime)
//                bundle1.putBoolean("activity", true)
//                bundle1.putInt("timer", timer)
//                var checkTimer = false
//                if (timer != 0)
//                    checkTimer = true
//                bundle1.putBoolean("checkTimer", checkTimer)
//                val intent = Intent(this, PlayActivity::class.java)
//                intent.putExtras(bundle1)
//                startActivity(intent)
//                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
//            }