package dd.wan.ddwanmediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.Podcast
import dd.wan.ddwanmediaplayer.model.ReadPodcast


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()

        var editSearch: EditText = findViewById(R.id.edit_search)
        var recyclerView: RecyclerView = findViewById(R.id.list_Podcast)
        var list = ReadPodcast(this).loadSong()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        var adapter = RecyclerAdapter(list)
        adapter.setCallback {

            var bundle = Bundle()
            bundle.putString("Uri", list[it].uri)
            bundle.putInt("type", 0)
            bundle.putInt("currentTime", 0)
            bundle.putInt("action", 0)

            val intent = Intent(this, MyService::class.java)
            intent.putExtras(bundle)
            startService(intent)


            var intent1 = Intent(this,PlayActivity::class.java)
            intent1.putExtras(bundle)
            startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))

        }
        recyclerView.adapter = adapter
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==123) {
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
            else {
                Toast.makeText(this,"Không có quyền truy cập bộ nhớ !!!",Toast.LENGTH_SHORT).show()
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
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                123
            )
        }
    }


}