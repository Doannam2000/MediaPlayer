package dd.wan.ddwanmediaplayer.activities

import android.app.ActivityManager
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
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_NEXT_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PLAY_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_PREVIOUS_SONG
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_STOP_SONG
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.service.Broadcast
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.activity_main.*
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.R
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var position = 0
    var currentTime = 0
    var check = true
    var listP = ArrayList<Podcast>()
    lateinit var adapter: RecyclerAdapter
    var timer = 0
    var activity = false
    var checkTimer = false



    val handle = Handler()
    val run = Runnable {
        val text = searchView.text
        list.clear()
        for (item in listP) {
            if (item.title.uppercase().contains(text.toString().uppercase())) {
                list.add(item)
            }
        }
        adapter.notifyDataSetChanged()
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

        val shared = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        timer = shared.getInt("timer", 0)
        val uri =
            shared.getString("Uri", "")
        if (uri != "" && uri!!.contains("/")) {
            for (i in list.indices) {
                if (list[i].uri == uri)
                    position = i
            }
            updateUI()
        }else if(uri!="" && !uri.contains("/")) {
            nameSong.text = shared.getString("nameSong","Tên bài hát")
            nameAuth.text = shared.getString("artists_names","Tên ca sĩ")
            Glide.with(applicationContext).load(shared.getString("thumbnail","")).into(imageP)
        }
        if (list.size != 0) {
            val bundle = intent.extras
            if (bundle != null) {
                val uri = bundle.getString("Uri")
                if (uri != null) {
                    layoutPodcast.visibility = View.VISIBLE
                    for (i in list.indices) {
                        if (list[i].uri == uri)
                            position = i
                    }
                    updateUI()
                    check = bundle.getBoolean("checked")
                    checkTimer = bundle.getBoolean("checkTimer")
                    timer = bundle.getInt("timer")
                    activity = bundle.getBoolean("activity")
                    if (check)
                        btnPlayN.setImageResource(R.drawable.ic_baseline_pause_24)
                    else
                        btnPlayN.setImageResource(R.drawable.ic_outline_play_arrow_24)
                }
            }
        }

        // lấy dữ liệu từ playActivity

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = RecyclerAdapter(list)
        adapter.setCallback {
            layoutPodcast.visibility = View.VISIBLE
            val podcast = list[it]
            position = it
            val bundle1 = Bundle()
            bundle1.putString("Uri", podcast.uri)
            bundle1.putInt("currentTime", 0)
            bundle1.putInt("action", 0)
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

        listP.addAll(list)

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                handle.removeCallbacks(run)
                handle.postDelayed(run, 500)
            }
        })

        btnExit.setOnClickListener {
            connectService(ACTION_STOP_SONG)
            layoutPodcast.visibility = View.GONE
        }
        btnNextN.setOnClickListener { connectService(ACTION_NEXT_SONG) }
        btnPrevious.setOnClickListener { connectService(ACTION_PREVIOUS_SONG) }
        btnPlayN.setOnClickListener { connectService(MyApplication.ACTION_PAUSE_OR_PLAY) }

        layoutPodcast.setOnClickListener {
            if (activity) {
                finish()
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
            } else {
                val podcast = list[position]
                val bundle1 = Bundle()
                bundle1.putString("Uri", podcast.uri)
                bundle1.putInt("currentTime", 0)
                bundle1.putInt("action", currentTime)
                bundle1.putBoolean("activity", true)
                bundle1.putInt("timer", timer)
                var checkTimer = false
                if (timer != 0)
                    checkTimer = true
                bundle1.putBoolean("checkTimer", checkTimer)
                val intent = Intent(this, PlayActivity::class.java)
                intent.putExtras(bundle1)
                startActivity(intent)
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
            }
        }

        updateUI()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))
        if(isMyServiceRunning(MyService::class.java))
        {
            connectService(MyApplication.ACTION_CHECK)
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

    private fun connectService(ac: Int) {
        val bundle = Bundle()
        bundle.putString("Uri", list[position].uri)
        bundle.putInt("currentTime", currentTime)
        bundle.putBoolean("online", false)
        if (isMyServiceRunning(MyService::class.java)) {
            bundle.putInt("action", ac)
            val intent = Intent(this, Broadcast::class.java)
            intent.putExtras(bundle)
            sendBroadcast(intent)
        } else {
            if (ac == MyApplication.ACTION_PAUSE_OR_PLAY)
                bundle.putInt("action", ACTION_PLAY_SONG)
            else
                bundle.putInt("action", ac)
            val intent = Intent(this, MyService::class.java)
            intent.putExtras(bundle)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPlay)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}