package dd.wan.ddwanmediaplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast
import kotlinx.android.synthetic.main.activity_play.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import android.widget.TimePicker

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NEXT_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NOT_REPEAT
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PAUSE_OR_PLAY
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PREVIOUS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_ALL
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_THIS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_TIMER
import kotlinx.android.synthetic.main.custom_editext_dialog.view.*
import java.util.*


class PlayActivity : AppCompatActivity() {

    private var type = 0
    var shuffle = false
    private var position = 0
    private var currentTime = 0
    private var list = ArrayList<Podcast>()
    var action = 0
    var check = true
    var timer = 0
    var activity = false

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
            timer = p1.extras!!.getInt("timer")
            if (timer != 0)
                btnClock.alpha = 1F
            else
                btnClock.alpha = 0.5F
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        list = ReadPodcast(this).loadSong()
        val bundle = intent.extras
        currentTime = bundle!!.getInt("currentTime")
        timer = bundle.getInt("timer")
        activity = bundle.getBoolean("activity")
        val uri = bundle.getString("Uri")
        for (i in 0 until list.size) {
            if (list[i].uri == uri)
                position = i
        }
        if (currentTime == 0)
            imageView.animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        if (timer != 0)
            btnClock.alpha = 1F
        else
            btnClock.alpha = 0.5F
        seekBar.progress = currentTime

        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        type = sharedPreferences.getInt("type", 0)
        shuffle = sharedPreferences.getBoolean("shuffle", false)
        when (type) {
            ACTION_REPEAT_ALL -> btnRepeat.setImageResource(R.drawable.repeat_all)
            ACTION_REPEAT_THIS_SONG -> {
                btnRepeat.setImageResource(R.drawable.repeat1)
            }
            ACTION_NOT_REPEAT -> {
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

        btnPlay.setOnClickListener { connectService(ACTION_PAUSE_OR_PLAY) }

        btnBack.setOnClickListener { connectService(ACTION_PREVIOUS_SONG) }

        btnNext.setOnClickListener { connectService(ACTION_NEXT_SONG) }

        btnRepeat.setOnClickListener {
            if (type == 2)
                type = 0
            else
                type++
            when (type) {
                ACTION_REPEAT_ALL -> btnRepeat.setImageResource(R.drawable.repeat_all)
                ACTION_REPEAT_THIS_SONG -> {
                    btnRepeat.setImageResource(R.drawable.repeat1)
                    shuffle = false
                    btnShuffle.alpha = 0.5F
                }
                ACTION_NOT_REPEAT -> {
                    btnRepeat.setImageResource(R.drawable.repeat)
                    shuffle = false
                    btnShuffle.alpha = 0.5F
                }
            }
            edit.putInt("type", type)
            edit.putBoolean("shuffle", shuffle)
            edit.commit()
            connectService(ACTION_CHANGE)
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
            edit.commit()
            connectService(ACTION_CHANGE)
        }

        btnBackward.setOnClickListener {
            if (currentTime > 15000) {
                seekBar.progress = currentTime - 10000
                currentTime -= 10000
                connectService(ACTION_PLAY_SONG)
            }
        }
        btnForward.setOnClickListener {
            if (currentTime + 10000 < list[position].duration) {
                seekBar.progress = currentTime + 10000
                currentTime += 10000
                connectService(ACTION_PLAY_SONG)
            }
        }
        previous.setOnClickListener {
            if (!activity) {
                val intent = Intent(this, MainActivity::class.java)
                val bundle1 = Bundle()
                bundle1.putString("Uri", list[position].uri)
                bundle1.putInt("type", type)
                bundle1.putInt("action", action)
                bundle1.putInt("currentTime", currentTime)
                bundle1.putBoolean("checked", check)
                bundle1.putBoolean("activity", true)
                bundle1.putInt("timer", timer)
                var checkTimer = false
                if (timer != 0)
                    checkTimer = true
                bundle1.putBoolean("checkTimer", checkTimer)
                intent.putExtras(bundle1)
                startActivity(intent)
                overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
            } else {
                finish()
                overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let {
                    currentTime = it
                    connectService(ACTION_PLAY_SONG)
                }
            }

        })

        btnClock.setOnClickListener {
            val bottom = BottomSheetDialog(this, R.style.bottomSheetDialog)
            bottom.setContentView(R.layout.custom_bottom_sheet)
            bottom.setCanceledOnTouchOutside(true)

            val time: TextView = bottom.findViewById(R.id.timePicker)!!
            val minute15: TextView = bottom.findViewById(R.id.minute_15)!!
            val minute30: TextView = bottom.findViewById(R.id.minute_30)!!
            val hour: TextView = bottom.findViewById(R.id.hour)!!
            val cancel: TextView = bottom.findViewById(R.id.cancel)!!
            val nameTimer: TextView = bottom.findViewById(R.id.nameTimer)!!

            minute15.setOnClickListener {
                timer = 15
                btnClock.alpha = 1F
                connectService(ACTION_TIMER)
                bottom.dismiss()
            }
            minute30.setOnClickListener {
                timer = 30
                btnClock.alpha = 1F
                connectService(ACTION_TIMER)
                bottom.dismiss()
            }
            hour.setOnClickListener {
                timer = 60
                btnClock.alpha = 1F
                connectService(ACTION_TIMER)
                bottom.dismiss()
            }
            cancel.setOnClickListener {
                bottom.dismiss()
            }

            time.setOnClickListener {
                if (timer == 0) {
                    val view = View.inflate(this, R.layout.custom_editext_dialog, null)
                    var builder = AlertDialog.Builder(this)
                    builder.setView(view)
                    val dialog = builder.create()
                    dialog.show()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view.cancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    view.oke.setOnClickListener {
                        dialog.dismiss()
                        if (view.settingMinute.isChecked) {
                            timer = view.settingTime.text.toString().toInt()
                        }
                        if (view.settingHour.isChecked) {
                            timer = 60 * view.settingTime.text.toString().toInt()
                        }
                        if (timer != 0)
                            btnClock.alpha = 1F
                        connectService(ACTION_TIMER)
                    }
                } else {
                    timer = 0
                    btnClock.alpha = 0.5F
                    connectService(ACTION_TIMER)
                }
                bottom.dismiss()
            }
            bottom.show()
            if (timer == 0) {
                btnClock.alpha = 0.5F
                nameTimer.text = "Hẹn giờ"
                time.text = "Lựa chọn"
            } else {
                time.text = "Hủy hẹn giờ"
                if (timer > 60) {
                    nameTimer.text = "${timer / 60} giờ ${timer % 60} phút"
                } else
                    nameTimer.text = "Hẹn giờ ( $timer phút )"
            }
        }
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
        bundle.putInt("action", ac)
        bundle.putInt("timer", timer)
        var checkTimer = false
        if (timer != 0)
            checkTimer = true
        bundle.putBoolean("checkTimer", checkTimer)
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