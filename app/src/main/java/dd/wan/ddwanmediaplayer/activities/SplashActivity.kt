package dd.wan.ddwanmediaplayer.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.sql.SQLHelper
import dd.wan.ddwanmediaplayer.viewmodel.MyViewModel

class SplashActivity : AppCompatActivity() {

    val model by lazy {
        ViewModelProvider(this).get(MyViewModel::class.java)
    }
    lateinit var sql: SQLHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        sql = SQLHelper(this)
        model.listOffline.observe(this, Observer {
            list = it
        })
        model.listFav.observe(this, Observer { data ->
            listFavorite = data
            list.forEach {
                listFavorite.add(FavoriteSong(it, "", false))
            }
        })
        requestPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                model.getPodcast(this)
                model.getFavoriteSQL(this)
            } else {
                model.getFavoriteSQL(this)
                Toast.makeText(applicationContext,
                    "Không có quyền truy cập dữ liệu trong máy !!!",
                    Toast.LENGTH_LONG).show()
            }
            Handler().postDelayed({
                startActivity(
                    Intent(
                        this,
                        MusicOnlineActivity::class.java
                    )
                )
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                finish()
            }, 800)
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
        } else {
            model.getPodcast(this)
            model.getFavoriteSQL(this)
            Handler().postDelayed({
                startActivity(
                    Intent(
                        this,
                        MusicOnlineActivity::class.java
                    )
                )
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                finish()
            }, 800)
        }
    }


}