package dd.wan.ddwanmediaplayer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_play.*


class MainActivity : AppCompatActivity() {
    var position = 0
    var list = ArrayList<Podcast>()
    var currentTime = 0
    var check = true
    var listP = ArrayList<Podcast>()
    lateinit var adapter: RecyclerAdapter
    var timer = 0
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
    var activity = false

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
                nameSong.text = list[position].title
                nameAuth.text = list[position].artist
                if (list[position].image.isNotEmpty()) {
                    val image = list[position].image
                    imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Không có quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show()
        } else
            list = ReadPodcast(this).loadSong()
        val searchView: TextView = findViewById(R.id.searchView)
        val recyclerView: RecyclerView = findViewById(R.id.list_Podcast)
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
                    nameSong.text = list[position].title
                    nameAuth.text = list[position].artist
                    if (list[position].image.isNotEmpty()) {
                        val image = list[position].image
                        imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                    }
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
            val bundle1 = Bundle()
            bundle1.putString("Uri", podcast.uri)
            bundle1.putInt("currentTime", 0)
            bundle1.putInt("action", 0)
            bundle1.putBoolean("activity", true)
            bundle1.putInt("timer", timer)
            var checkTimer = false

            nameSong.text = podcast.title
            nameAuth.text = podcast.artist
            if (podcast.image.isNotEmpty()) {
                val image = podcast.image
                imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
            }

            if (timer != 0)
                checkTimer = true
            bundle1.putBoolean("checkTimer", checkTimer)

            val intent11 = Intent(this, MyService::class.java)
            intent11.putExtras(bundle1)
            startService(intent11)

            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtras(bundle1)
            startActivity(intent)
            overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left_out)
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
            connectService(4)
            layoutPodcast.visibility = View.GONE
        }
        btnNextN.setOnClickListener { connectService(3) }
        btnPrevious.setOnClickListener { connectService(1) }
        btnPlayN.setOnClickListener { connectService(2) }
        layoutPodcast.setOnClickListener {
            if (activity) {
                finish()
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
            }else {
                val podcast = list[position]
                val bundle1 = Bundle()
                bundle1.putString("Uri", podcast.uri)
                bundle1.putInt("currentTime", 0)
                bundle1.putInt("action", 0)
                bundle1.putBoolean("activity", true)
                bundle1.putInt("timer", timer)
                var checkTimer = false
                if (timer != 0)
                    checkTimer = true
                bundle1.putBoolean("checkTimer", checkTimer)
                val intent = Intent(this, PlayActivity::class.java)
                intent.putExtras(bundle1)
                startActivity(intent)
                overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left_out)
            }
        }


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlay, IntentFilter("Pause_Play"))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                Toast.makeText(this, "Không có quyền truy cập bộ nhớ !!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        }
    }

    private fun connectService(ac: Int) {
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
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPodcast)
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPlay)
    }

}