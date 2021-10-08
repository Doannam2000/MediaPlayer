package dd.wan.ddwanmediaplayer

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.View
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var position = 0
    var type = 0
    var list = ArrayList<Podcast>()
    var currentTime = 0
    var check = true


    private val broadcastPodcast = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            var bundle = p1?.extras
            if (bundle == null)
                return
            else {
                currentTime = bundle.getInt("currentTime")
                type = bundle.getInt("type")
                var uri = bundle.getString("Uri") as String
                for (i in 0 until list.size) {
                    if (list[i].uri == uri)
                        position = i
                }
                check = bundle.getBoolean("check")
                if (check)
                    btnPlayN.setImageResource(R.drawable.ic_baseline_pause_24)
                else
                    btnPlayN.setImageResource(R.drawable.ic_outline_play_arrow_24)
                nameSong.text = list[position].title
                nameAuth.text = list[position].artist
                if (list[position].image.isNotEmpty()) {
                    var image = list[position].image
                    imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image!!.size))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        list = ReadPodcast(this).loadSong()
        var editSearch: EditText = findViewById(R.id.edit_search)
        var recyclerView: RecyclerView = findViewById(R.id.list_Podcast)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPodcast, IntentFilter("Current_Song"))
        val bundle = intent.extras
        if (bundle != null) {
            layoutPodcast.visibility = View.VISIBLE
            var uri = bundle.getString("Uri")
            for (i in list.indices) {
                if (list[i].uri == uri)
                    position = i
            }
            nameSong.text = list[position].title
            nameAuth.text = list[position].artist
            if (list[position].image.isNotEmpty()) {
                var image = list[position].image
                imageP.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image!!.size))
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        var adapter = RecyclerAdapter(list)
        adapter.setCallback {
            var podcast = list[it]
            var bundle = Bundle()
            bundle.putString("Uri", podcast.uri)
            bundle.putInt("type", type)
            bundle.putInt("currentTime", currentTime)
            bundle.putInt("action", 0)

            val intent = Intent(this, MyService::class.java)
            intent.putExtras(bundle)
            startService(intent)

            var intent1 = Intent(this, PlayActivity::class.java)
            intent1.putExtras(bundle)
            startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
        recyclerView.adapter = adapter

        btnExit.setOnClickListener {
            connectService(4)
        }
        btnNextN.setOnClickListener { connectService(3) }
        btnPrevious.setOnClickListener { connectService(1) }
        btnPlayN.setOnClickListener { connectService(2) }
        layoutPodcast.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 123) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    fun requestPermission() {
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

    fun connectService(ac: Int) {
        var intent = Intent(this, Broadcast::class.java)
        var bundle = Bundle()
        bundle.putString("Uri", list[position].uri)
        bundle.putInt("type", type)
        bundle.putInt("action", ac)
        bundle.putInt("currentTime", currentTime)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastPodcast)
    }
}