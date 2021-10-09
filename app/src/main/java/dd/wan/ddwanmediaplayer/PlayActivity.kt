package dd.wan.ddwanmediaplayer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast
import kotlinx.android.synthetic.main.activity_play.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class PlayActivity : AppCompatActivity() {

    private var type = 0
    var shuffle = false
    private var position = 0
    private var currentTime = 0
    private var list = ArrayList<Podcast>()
    var action = 0
    var check = true

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("mm:ss")


    private val broadcastPosition = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle = p1?.extras
            if (bundle == null)
                return
            else {
                currentTime = bundle.getInt("currentPosition")
                seekBar.progress = currentTime
                time1.text = sdf.format(currentTime)
            }
        }
    }

    private val broadcastPodcast = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle = p1?.extras
            if (bundle == null)
                return
            else {
                currentTime = bundle.getInt("currentTime")
                seekBar.progress = currentTime
                type = bundle.getInt("type")
                action = bundle.getInt("action")
                val uri = bundle.getString("Uri") as String
                for (i in 0 until list.size) {
                    if (list[i].uri == uri)
                        position = i
                }
                updateUI()
                time1.text = sdf.format(currentTime)
            }
        }
    }

    private val broadcastPlay = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            check = p1!!.extras!!.getBoolean("checked")
            if (check)
                btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            else
                btnPlay.setImageResource(R.drawable.ic_outline_play_arrow_24)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        list = ReadPodcast(this).loadSong()
        val bundle = intent.extras
        currentTime = bundle!!.getInt("currentTime")
        seekBar.progress = currentTime
        val uri = bundle.getString("Uri")
        for (i in 0 until list.size) {
            if (list[i].uri == uri)
                position = i
        }
        if (currentTime == 0)
            imageView.animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)

        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        type = sharedPreferences.getInt("type", 0)
        shuffle = sharedPreferences.getBoolean("shuffle", false)
        when (type) {
            0 -> btnRepeat.setImageResource(R.drawable.repeat_all)
            1 -> {
                btnRepeat.setImageResource(R.drawable.repeat1)
            }
            2 -> {
                btnRepeat.setImageResource(R.drawable.repeat)
            }
        }
        if (shuffle)
            btnShuffle.alpha = 1F
        else
            btnShuffle.alpha = 0.5F

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPosition, IntentFilter("Current_Position"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        updateUI()

        btnPlay.setOnClickListener { connectService(2) }

        btnBack.setOnClickListener { connectService(1) }

        btnNext.setOnClickListener { connectService(3) }

        btnRepeat.setOnClickListener {
            if (type == 2)
                type = 0
            else
                type++
            when (type) {
                0 -> btnRepeat.setImageResource(R.drawable.repeat_all)
                1 -> {
                    btnRepeat.setImageResource(R.drawable.repeat1)
                    shuffle = false
                    btnShuffle.alpha = 0.5F
                }
                2 -> {
                    btnRepeat.setImageResource(R.drawable.repeat)
                    shuffle = false
                    btnShuffle.alpha = 0.5F
                }
            }
            edit.putInt("type", type)
            edit.putBoolean("shuffle", shuffle)
            edit.apply()
            connectService(5)
        }
        btnShuffle.setOnClickListener {
            if (shuffle) {
                shuffle = false
                btnShuffle.alpha = 0.5F
            } else {
                shuffle = true
                btnShuffle.alpha = 1F
                btnRepeat.setImageResource(R.drawable.repeat_all)
                type = 0
            }
            edit.putBoolean("shuffle", shuffle)
            edit.putInt("type", type)
            edit.apply()
            connectService(5)
        }

        btnBackward.setOnClickListener {
            if (currentTime > 15000) {
                seekBar.progress = currentTime - 10000
                currentTime -= 10000
                connectService(0)
            }
        }
        btnForward.setOnClickListener {
            if (currentTime + 10000 < list[position].duration) {
                seekBar.progress = currentTime + 10000
                currentTime += 10000
                connectService(0)
            }
        }
        previous.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val bundle1 = Bundle()
            bundle1.putString("Uri", list[position].uri)
            bundle1.putInt("type", type)
            bundle1.putInt("action", action)
            bundle1.putInt("currentTime", currentTime)
            bundle1.putBoolean("checked", check)
            intent.putExtras(bundle1)
            startActivity(intent)
            overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let {
                    currentTime = it
                    connectService(0)
                }
            }

        })
    }

    fun updateUI() {
        if (currentTime == 0) {
            if (list[position].image.isNotEmpty()) {
                val image = list[position].image
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
            } else {
                imageView.setImageResource(R.drawable.music_icon)
            }
        }
        name1.text = list[position].title
        val string = "${list[position].title} \n\n ${list[position].artist}"
        name.text = string

        btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
        time2.text = sdf.format(list[position].duration)
        seekBar.max = list[position].duration
    }

    fun connectService(ac: Int) {
        val intent = Intent(this, Broadcast::class.java)
        val bundle = Bundle()
        bundle.putString("Uri", list[position].uri)
        bundle.putInt("action", ac)
        bundle.putInt("currentTime", currentTime)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
//        connectService(6) tắt handle khi tắt màn
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPosition)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlay)
    }
}