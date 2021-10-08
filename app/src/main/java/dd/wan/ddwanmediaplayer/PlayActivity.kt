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
                type = bundle.getInt("type")
                action = bundle.getInt("action")
                val uri = bundle.getString("Uri") as String
                for (i in 0 until list.size) {
                    if (list[i].uri == uri)
                        position = i
                }
                updateUI()
                seekBar.progress = currentTime
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
        val uri = bundle.getString("Uri")
        for (i in 0 until list.size) {
            if (list[i].uri == uri)
                position = i
        }
        imageView.animation = AnimationUtils.loadAnimation(this,R.anim.anim_rotate)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPosition, IntentFilter("Current_Position"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        updateUI()
        btnPlay.setOnClickListener {
            connectService(2)
        }

        btnBack.setOnClickListener {
            connectService(1)
        }

        btnNext.setOnClickListener {
            connectService(3)
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
        setting.setOnClickListener { it ->
            val pop = PopupMenu(this, it)
            pop.menuInflater.inflate(R.menu.menu, pop.menu)
            pop.show()
            pop.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.repeatAll -> type = 0
                    R.id.repeat -> type = 1
                    R.id.shuffle -> type = 2
                    R.id.noRepeat -> type = 3
                }
                connectService(5)
                false
            }
        }
        previous.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val bundle1 = Bundle()
            bundle1.putString("Uri", list[position].uri)
            bundle1.putInt("type", type)
            bundle1.putInt("action", action)
            bundle1.putInt("currentTime", currentTime)
            bundle1.putBoolean("checked",check)
            intent.putExtras(bundle1)
            startActivity(intent)
            overridePendingTransition(R.anim.left_to_right,R.anim.left_to_right_out)
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
        if (list[position].image.isNotEmpty()) {
            val image = list[position].image
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
        } else {
            imageView.setImageResource(R.drawable.music_icon)
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
        bundle.putInt("type", type)
        bundle.putInt("action", ac)
        bundle.putInt("currentTime", currentTime)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPosition)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlay)
    }
}