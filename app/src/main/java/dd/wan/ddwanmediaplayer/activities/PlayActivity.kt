package dd.wan.ddwanmediaplayer.activities

import android.app.AlertDialog
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_play.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NOT_REPEAT
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_ALL
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_REPEAT_THIS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_TIMER
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import kotlinx.android.synthetic.main.custom_editext_dialog.view.*
import android.content.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PAUSE_OR_PLAY
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.config.Constants.Companion.activity
import dd.wan.ddwanmediaplayer.config.Constants.Companion.connectService
import dd.wan.ddwanmediaplayer.config.Constants.Companion.timer
import dd.wan.ddwanmediaplayer.config.Constants.Companion.sdf
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService


class PlayActivity : AppCompatActivity() {

    private var type = 0
    var shuffle = false
    var action = ACTION_PLAY_SONG
    lateinit var mySerVice: MyService
    var bound = false
    var handler = Handler()
    var run = object : Runnable {
        override fun run() {
            if (mySerVice.mediaPlayer.isPlaying) {
                currentTime = mySerVice.mediaPlayer.currentPosition
                seekBar.progress = mySerVice.mediaPlayer.currentPosition
                time1.text = sdf.format(mySerVice.mediaPlayer.currentPosition)
            }
            handler.postDelayed(this, 500)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyService.MyBinder
            mySerVice = binder.getService()
            handler.postDelayed(run, 100)
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            handler.removeCallbacks(run)
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MyService::class.java), connection, Context.BIND_AUTO_CREATE)
        if (this::mySerVice.isInitialized) {
            time2.text = sdf.format(mySerVice.mediaPlayer.duration)
            seekBar.max = mySerVice.mediaPlayer.duration
        }
    }

    private val broadcastPodcast = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle = p1?.extras
            if (bundle == null)
                return
            else {
                seekBar.progress = currentTime
                type = bundle.getInt("type")
                action = bundle.getInt("action")
                if (online) {
                    val song = bundle.getSerializable("Song") as Song
                    for (i in 0 until listRecommendMusic.size) {
                        if (listRecommendMusic[i].id == song.id)
                            position = i
                    }
                } else {
                    val uri = bundle.getString("Uri")
                    for (i in 0 until list.size) {
                        if (list[i].uri == uri)
                            position = i
                    }
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
            if (p1.extras!!.getBoolean("exit")) {
                if (!activity) {
                    startActivity(Intent(p0, MusicOnlineActivity::class.java))
                    overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
                    finish()
                } else {
                    finish()
                    overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        timer = sharedPreferences.getInt("timer", 0)
        type = sharedPreferences.getInt("type", 0)
        shuffle = sharedPreferences.getBoolean("shuffle", false)


        imageView.animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)

        if (timer != 0)
            btnClock.alpha = 1F
        else
            btnClock.alpha = 0.5F

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
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        seekBar.progress = currentTime

        btnPlay.setOnClickListener {
            mySerVice.playOr()
        }

        btnBack.setOnClickListener {
            if (online) {
                if (mySerVice.arrayPlayed.size == listRecommendMusic.size) {
                    mySerVice.arrayPlayed.clear()
                }
            } else {
                if (mySerVice.arrayPlayed.size == list.size) {
                    mySerVice.arrayPlayed.clear()
                }
            }
            mySerVice.previous()
        }

        btnNext.setOnClickListener {
            if (online) {
                if (mySerVice.arrayPlayed.size == listRecommendMusic.size) {
                    mySerVice.arrayPlayed.clear()
                }
            } else {
                if (mySerVice.arrayPlayed.size == list.size) {
                    mySerVice.arrayPlayed.clear()
                }
            }
            mySerVice.nextSong()

        }

        btnRepeat.setOnClickListener {
            if (type == ACTION_NOT_REPEAT)
                type = ACTION_REPEAT_ALL
            else
                type++
            when (type) {
                ACTION_REPEAT_ALL -> btnRepeat.setImageResource(R.drawable.repeat_all)
                ACTION_REPEAT_THIS_SONG -> {
                    btnRepeat.setImageResource(R.drawable.repeat1)
                }
                ACTION_NOT_REPEAT -> {
                    btnRepeat.setImageResource(R.drawable.repeat)
                }
            }
            edit.putInt("type", type)
            edit.commit()
            mySerVice.type = type
        }
        btnShuffle.setOnClickListener {
            if (shuffle) {
                shuffle = false
                btnShuffle.alpha = 0.5F
            } else {
                shuffle = true
                btnShuffle.alpha = 1F
            }
            edit.putBoolean("shuffle", shuffle)
            edit.commit()
            mySerVice.shuffle = shuffle
        }


        btnBackward.setOnClickListener {
            if (currentTime > 15000) {
                seekBar.progress = currentTime - 10000
                currentTime -= 10000
                mySerVice.mediaPlayer.seekTo(currentTime)
            }
        }
        btnForward.setOnClickListener {
            var duration =
                if (online)
                    listRecommendMusic[position].duration * 1000
                else
                    list[position].duration
            if (currentTime + 10000 < duration) {
                seekBar.progress = currentTime + 10000
                currentTime += 10000

                mySerVice.mediaPlayer.seekTo(currentTime)

            }
        }
        previous.setOnClickListener {
            activity = if (activity) {
                finish()
                overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
                false
            } else {
                startActivity(Intent(this, MusicOnlineActivity::class.java))
                overridePendingTransition(R.anim.left_to_right, R.anim.left_to_right_out)
                true
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let {
                    mySerVice.mediaPlayer.seekTo(it)
                }
            }

        })
        updateUI()
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
                getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).edit()
                    .putInt("timer", timer).apply()
                connectService(ACTION_TIMER, this)
                bottom.dismiss()
            }
            minute30.setOnClickListener {
                timer = 30
                btnClock.alpha = 1F
                getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).edit()
                    .putInt("timer", timer).apply()
                connectService(ACTION_TIMER, this)
                bottom.dismiss()
            }
            hour.setOnClickListener {
                timer = 60
                btnClock.alpha = 1F
                getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).edit()
                    .putInt("timer", timer).apply()
                connectService(ACTION_TIMER, this)
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
                        getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).edit()
                            .putInt("timer", timer).apply()
                        connectService(ACTION_TIMER, this)
                    }
                } else {
                    timer = 0
                    btnClock.alpha = 0.5F
                    getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE).edit()
                        .putInt("timer", timer).apply()
                    connectService(ACTION_TIMER, this)
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
        if (online) {
            time1.text = sdf.format(currentTime)
            name1.text = listRecommendMusic[position].name
            name.text =
                listRecommendMusic[position].name + "\n\n" + listRecommendMusic[position].artists_names
            var linkImg  = listRecommendMusic[position].thumbnail.removeRange(34,48)
            Glide.with(this).load(linkImg).into(imageView)
            time2.text = sdf.format(listRecommendMusic[position].duration * 1000)
            seekBar.max = listRecommendMusic[position].duration * 1000
        } else {
            if (list[position].image.isNotEmpty()) {
                try {
                    val image = list[position].image
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                } catch (e: Exception) {
                    imageView.setImageResource(R.drawable.music_icon)
                }
            } else {
                imageView.setImageResource(R.drawable.music_icon)
            }
            time1.text = sdf.format(currentTime)
            name1.text = list[position].title
            val string = "${list[position].title} \n\n ${list[position].artist}"
            name.text = string
            time2.text = sdf.format(list[position].duration)
            seekBar.max = list[position].duration
        }
        if (check || this::mySerVice.isInitialized && mySerVice.mediaPlayer.isPlaying)
            btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
        else
            btnPlay.setImageResource(R.drawable.ic_outline_play_arrow_24)
    }


    override fun onStop() {
        super.onStop()
        unbindService(connection)
        bound = false
        handler.removeCallbacks(run)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlay)
    }


}