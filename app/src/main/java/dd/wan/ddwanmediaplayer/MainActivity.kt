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


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()

        var editSearch: EditText = findViewById(R.id.edit_search)
        var recyclerView: RecyclerView = findViewById(R.id.list_Podcast)
        var list = loadSong()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        var adapter = RecyclerAdapter(list)
        adapter.setCallback {
            var intent = Intent(this,PlayActivity::class.java)
            intent.putExtra("position",it)
            startActivity(intent)
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
    @SuppressLint("Range")
    fun loadSong(): ArrayList<Podcast> {
        var list = ArrayList<Podcast>()
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var rs = this.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID),
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )
        if (rs != null) {
            while (rs!!.moveToNext()) {
                var uri = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DATA))
                var title = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.TITLE))
                var artist = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                var media = MediaMetadataRetriever()
                media.setDataSource(uri)
                var bitmap:ByteArray? = media.embeddedPicture
                if(bitmap==null)
                {
                    bitmap = byteArrayOf()
                }
                list.add(Podcast(uri, title,artist,bitmap))
            }
        }
        return list
    }

}