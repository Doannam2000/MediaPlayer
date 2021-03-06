package dd.wan.ddwanmediaplayer.activities

import android.app.AlertDialog
import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import kotlinx.android.synthetic.main.custom_editext_dialog.view.*
import android.content.*
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataFragToAct
import dd.wan.ddwanmediaplayer.adapter.ViewPagerAdapter
import dd.wan.ddwanmediaplayer.config.Constants.Companion.online
import dd.wan.ddwanmediaplayer.config.Constants.Companion.check
import dd.wan.ddwanmediaplayer.config.Constants.Companion.position
import dd.wan.ddwanmediaplayer.config.Constants.Companion.activity
import dd.wan.ddwanmediaplayer.config.Constants.Companion.connectService
import dd.wan.ddwanmediaplayer.config.Constants.Companion.timer
import dd.wan.ddwanmediaplayer.config.Constants.Companion.sdf
import dd.wan.ddwanmediaplayer.config.Constants.Companion.currentTime
import dd.wan.ddwanmediaplayer.config.Constants.Companion.isFavorite
import dd.wan.ddwanmediaplayer.config.Constants.Companion.song
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import dd.wan.ddwanmediaplayer.sql.SQLHelper
import dd.wan.ddwanmediaplayer.viewmodel.MyViewModel


class PlayActivity : AppCompatActivity(), DataFragToAct {

    private var type = 0
    var shuffle = false
    var action = ACTION_PLAY_SONG
    lateinit var mySerVice: MyService
    var bound = false
    var handler = Handler()
    var bundle = Bundle()
    var download = 0L
    var checkFavorite = false
    var checkDownload = true
    private var dataListener: OnDataReceivedListener? = null
    val model by lazy {
        ViewModelProvider(this).get(MyViewModel::class.java)
    }

    interface OnDataReceivedListener {
        fun onDataReceived(song: Song, position: Int, online: Boolean, isFav: Boolean)
    }

    fun setListener(listener: OnDataReceivedListener?) {
        dataListener = listener
    }



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
                action = bundle.getInt("action")
                updateUI()
                seekBar.progress = currentTime
                time1.text = sdf.format(currentTime)
                dataListener!!.onDataReceived(song, position, online, isFavorite)
                checkDownload()
                if(!checkDownload){
                    btnFav.isEnabled = false
                    btnFav.alpha = 0.5F
                }
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

    private val broadcastDownload = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == download) {
                Toast.makeText(applicationContext, "T???i xu???ng th??nh c??ng", Toast.LENGTH_SHORT)
                    .show()
                model.getPodcast(applicationContext)
                SQLHelper(applicationContext).deleteDB(song.id)
                btnFav.isEnabled = false
                btnFav.alpha = 0.5F
                checkDownload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        supportActionBar?.hide()
        val sharedPreferences = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        timer = sharedPreferences.getInt("timer", 0)
        type = sharedPreferences.getInt("type", 0)
        shuffle = sharedPreferences.getBoolean("shuffle", false)

        changeDataFragment()
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, bundle)
        viewPager.adapter = adapter

        dataListener?.onDataReceived(song, position, online, isFavorite)

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


        seekBar.progress = currentTime

        btnPlay.setOnClickListener {
            mySerVice.playOr()
        }

        btnDownload.setOnClickListener {
            if (checkDownload) {
                if ((online && !isFavorite) || (isFavorite && listFavorite[position].isOnline)) {
                    Toast.makeText(this, "??ang t???i xu???ng", Toast.LENGTH_SHORT).show()
                    val ur = "https://api.mp3.zing.vn/api/streaming/audio/${song.id}/320"
                    val name = song.name.filter { it.isLetterOrDigit() } + "_" + song.id + ".mp3"
                    val request = DownloadManager.Request(Uri.parse(ur))
                        .setTitle(name)
                        .setDescription(song.name)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(false)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,name)
                    val downloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    download = downloadManager.enqueue(request)
                    checkDownload = false
                }
            } else
                Toast.makeText(this, "B??i h??t ???? c?? trong m??y", Toast.LENGTH_SHORT).show()
        }

        btnList.setOnClickListener { viewPager.currentItem = 1 }
        btnBack.setOnClickListener {
            mySerVice.clearArrayPlayed()
            mySerVice.previous()
            dataListener!!.onDataReceived(song, position, online, isFavorite)
        }

        btnNext.setOnClickListener {
            mySerVice.clearArrayPlayed()
            mySerVice.nextSong()
            dataListener!!.onDataReceived(song, position, online, isFavorite)
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
            edit.apply()
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
            edit.apply()
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
            val duration =
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
        checkDownload()
        // setUp H???n gi???
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
                    val builder = AlertDialog.Builder(this)
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
                nameTimer.text = "H???n gi???"
                time.text = "L???a ch???n"
            } else {
                time.text = "H???y h???n gi???"
                if (timer > 60) {
                    nameTimer.text = "${timer / 60} gi??? ${timer % 60} ph??t"
                } else
                    nameTimer.text = "H???n gi??? ( $timer ph??t )"
            }
        }


        // setUp favorite


        btnFav.setOnClickListener {
            if (checkFavorite) {
                btnFav.alpha = 0.5F
                checkFavorite = false
                for (i in listFavorite.indices) {
                    if (listFavorite[i].song.uri == song.id) {
                        listFavorite.removeAt(i)
                        break
                    }
                }
                SQLHelper(this).deleteDB(song.id)
                Toast.makeText(this,
                    "???? b??? b??i h??t ra kh???i danh s??ch y??u th??ch",
                    Toast.LENGTH_SHORT).show()
            } else {
                val favoriteSong = FavoriteSong(Podcast(song.id, song.name, song.artists_names,
                    byteArrayOf(), song.duration, ""), song.thumbnail, true)
                SQLHelper(this).insertDB(favoriteSong)
                listFavorite.add(favoriteSong)
                btnFav.alpha = 1F
                checkFavorite = true
                Toast.makeText(this, "???? th??m b??i h??t v??o danh s??ch y??u th??ch", Toast.LENGTH_SHORT)
                    .show()
            }
            if (isFavorite) {
                viewPager.currentItem = 0
            }
        }

        model.listOffline.observe(this, Observer {
            list = it
        })

        // register broadcast
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        registerReceiver(broadcastDownload, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }


    private fun checkFavorite(): Boolean {
        listFavorite.forEach {
            if (it.song.uri == listRecommendMusic[position].id) {
                checkFavorite = true
                return true
            }
        }
        checkFavorite = false
        return false
    }

    fun updateUI() {
        if ((isFavorite && listFavorite[position].isOnline) || (online && !isFavorite)) {
            setUIOnline()
            btnFav.isEnabled = true
            if (checkFavorite())
                btnFav.alpha = 1F
            else
                btnFav.alpha = 0.5F
        } else {
            if (isFavorite)
                setUIOffline(listFavorite[position].song)
            else
                setUIOffline(list[position])
            btnFav.alpha = 0.5F
            btnFav.isEnabled = false
        }

        if (check || this::mySerVice.isInitialized && mySerVice.mediaPlayer.isPlaying)
            btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
        else
            btnPlay.setImageResource(R.drawable.ic_outline_play_arrow_24)
    }

    private fun setUIOnline() {
        time1.text = sdf.format(currentTime)
        name1.text = song.name
        time2.text = sdf.format(song.duration * 1000)
        seekBar.max = song.duration * 1000
    }

    private fun setUIOffline(podcast: Podcast) {
        time1.text = sdf.format(currentTime)
        name1.text = podcast.title
        time2.text = sdf.format(podcast.duration)
        seekBar.max = podcast.duration
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

    private fun changeDataFragment() {
        bundle = Bundle()
        bundle.putBoolean("online", online)
        if (online) {
            bundle.putSerializable("song", song)
            bundle.putSerializable("listRecommend", listRecommendMusic)
        } else {
            bundle.putInt("position", position)
        }
        bundle.putBoolean("isFav", isFavorite)
    }

    override fun sendData(song1: Song, position1: Int, online1: Boolean, isFav: Boolean) {
        currentTime = 0
        song = song1
        position = position1
        online = online1
        isFavorite = isFav
        mySerVice.play()
        updateUI()
        checkDownload()
        if(!checkDownload){
            btnFav.isEnabled = false
            btnFav.alpha = 0.5F
        }
        dataListener!!.onDataReceived(song, position, online, isFavorite)
        viewPager.currentItem = 0
    }


    override fun onBackPressed() {
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

    fun checkDownload() {
        list.forEach {
            if (it.uri.contains(song.id)) {
                checkDownload = false
                return
            }
        }
        checkDownload = true
    }

}