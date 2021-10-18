package dd.wan.ddwanmediaplayer.activities

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.ACTION_CHANGE
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.CallAPI.Companion.callApi
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.model.recommend.RecommendMusic
import dd.wan.ddwanmediaplayer.model.top.Music
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.Broadcast
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.activity_music_online.*
import kotlinx.android.synthetic.main.activity_music_online.btnExit
import kotlinx.android.synthetic.main.activity_music_online.btnNextN
import kotlinx.android.synthetic.main.activity_music_online.btnPlayN
import kotlinx.android.synthetic.main.activity_music_online.btnPrevious
import kotlinx.android.synthetic.main.activity_music_online.imageP
import kotlinx.android.synthetic.main.activity_music_online.nameAuth
import kotlinx.android.synthetic.main.activity_music_online.nameSong
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MusicOnlineActivity : AppCompatActivity() {

    var listSong = ArrayList<Song>()
    lateinit var adapter: RecyclerMusicAdapter
    var listRecommendMusic = ArrayList<Song>()
    var currentTime = 0
    var activity = false
    var check = false
    var song = Song()
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_online)
        adapter = RecyclerMusicAdapter(listSong)
        adapter.setCallback {
            layout_music_play.visibility = View.VISIBLE
            updateUI(listSong[it])
            song = listSong[it]
            listRecommendMusic.clear()
            listRecommendMusic.add(song)
            getRecommendSong(song.id,true,startSer = true)
        }

        listMusicOnline.adapter = adapter
        listMusicOnline.layoutManager = LinearLayoutManager(this)
        getData()

        val bundle = intent.extras
        if (bundle != null) {
            val song = bundle.getSerializable("Song") as Song
            listRecommendMusic.clear()
            listRecommendMusic.add(song)
            getRecommendSong(song.id,false, startSer = false)
            layout_music_play.visibility = View.VISIBLE
            updateUI(song)
            check = bundle.getBoolean("checked")
            activity = bundle.getBoolean("activity")
            currentTime = bundle.getInt("currentTime")
            if (check)
                btnPlayN.setImageResource(R.drawable.ic_baseline_pause_24)
            else
                btnPlayN.setImageResource(R.drawable.ic_outline_play_arrow_24)
        }

        val shared = getSharedPreferences("SHARE_PREFERENCES", Context.MODE_PRIVATE)
        val uri = shared.getString("Uri", "")
        if (uri != "" && uri!!.contains("/")) {
            for (i in MyApplication.list.indices) {
                if (MyApplication.list[i].uri == uri)
                    position = i
            }
            nameSong.text = MyApplication.list[position].title
            nameAuth.text = MyApplication.list[position].artist
            if (MyApplication.list[position].image.isNotEmpty()) {
                try {
                    val image = MyApplication.list[position].image
                    imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                } catch (e: Exception) {
                    imageP.setImageResource(R.drawable.music_icon)
                }
            } else {
                imageP.setImageResource(R.drawable.music_icon)
            }
        } else if (uri != "" && !uri.contains("/")) {
            song.id = uri
            listRecommendMusic.clear()
            listRecommendMusic.add(song)
            getRecommendSong(song.id,false, startSer = false)
            song.name = shared.getString("nameSong", "Tên bài hát")!!
            song.artists_names = shared.getString("artists_names", "Tên ca sĩ")!!
            song.thumbnail = shared.getString("thumbnail", "")!!
            song.duration = shared.getInt("duration", 0)
            nameSong.text = shared.getString("nameSong", "Tên bài hát")
            nameAuth.text = shared.getString("artists_names", "Tên ca sĩ")
            Glide.with(applicationContext).load(shared.getString("thumbnail", "")).into(imageP)
        }

        btnExit.setOnClickListener {
            connectService(MyApplication.ACTION_STOP_SONG)
            layout_music_play.visibility = View.GONE
        }
        btnNextN.setOnClickListener { connectService(MyApplication.ACTION_NEXT_SONG) }
        btnPrevious.setOnClickListener { connectService(MyApplication.ACTION_PREVIOUS_SONG) }
        btnPlayN.setOnClickListener { connectService(MyApplication.ACTION_PAUSE_OR_PLAY) }

        if (isMyServiceRunning(MyService::class.java)) {
            connectService(MyApplication.ACTION_CHECK)
        }

        layout_music_play.setOnClickListener {
            if (activity) {
                finish()
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
            } else {
                getRecommendSong(song.id,true, startSer = false)
                if(!isMyServiceRunning(MyService::class.java))
                    connectService(ACTION_CHANGE)
            }
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))

    }


    private fun getRecommendSong(id: String,startAc:Boolean,startSer: Boolean) {
        val retrofit = callApi.getRecommendSong("audio", id)
        retrofit.enqueue(object : Callback<RecommendMusic> {
            override fun onResponse(
                call: Call<RecommendMusic>,
                response: Response<RecommendMusic>) {
                val responseBody = response.body()!!
                listRecommendMusic.addAll(responseBody.data.items)
                if(startAc) {
                    val bundle = Bundle()
                    bundle.putInt("action", MyApplication.ACTION_PLAY_SONG)
                    bundle.putBoolean("online", true)
                    bundle.putBoolean("checked", true)
                    bundle.putString("Uri", id)
                    bundle.putInt("currentTime", currentTime)
                    bundle.putBoolean("activity", true)
                    bundle.putSerializable("listRecommendMusic", listRecommendMusic)
                    if(startSer)
                    {
                        val intent = Intent(this@MusicOnlineActivity, MyService::class.java)
                        intent.putExtras(bundle)
                        startService(intent)
                    }
                    val intent1 = Intent(this@MusicOnlineActivity, PlayActivity::class.java)
                    intent1.putExtras(bundle)
                    startActivity(intent1)
                    overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                }
            }

            override fun onFailure(call: Call<RecommendMusic>, t: Throwable) {
                Log.d("dulieu", "Đếch call được")
            }
        })
    }


    private fun getData() {
        val retrofitData = callApi.getTopMusic(0, 0, 0, "song", -1)
        retrofitData.enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                val responseBody = response.body()!!
                listSong.addAll(responseBody.data.song)
                progressBar.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Music>, t: Throwable) {
                Log.d("dulieu", "Đếch call được")
            }
        })
    }


    private fun updateUI(song: Song) {
        nameSong.text = song.name
        nameAuth.text = song.artists_names
        Glide.with(applicationContext).load(song.thumbnail).into(imageP)
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
                song = bundle.getSerializable("Song") as Song
                updateUI(song)
            }
        }
    }

    private fun connectService(ac: Int) {
        val bundle = Bundle()
        bundle.putString("Uri", song.id)
        bundle.putBoolean("online", true)
        bundle.putInt("currentTime", currentTime)
        bundle.putSerializable("listRecommendMusic", listRecommendMusic)
        if (isMyServiceRunning(MyService::class.java)) {
            bundle.putInt("action", ac)
            val intent = Intent(this, Broadcast::class.java)
            intent.putExtras(bundle)
            sendBroadcast(intent)
        } else {
            if (ac == MyApplication.ACTION_PAUSE_OR_PLAY)
                bundle.putInt("action", MyApplication.ACTION_PLAY_SONG)
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