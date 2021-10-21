package dd.wan.ddwanmediaplayer.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.config.Constants.Companion.updateDataFromSdcard
import dd.wan.ddwanmediaplayer.sql.SQLHelper
import kotlin.system.exitProcess

class SplashActivity : AppCompatActivity() {
    lateinit var sql:SQLHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sql = SQLHelper(this)
        requestPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray, ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateDataFromSdcard(applicationContext)
                Handler().postDelayed({
                    startActivity(
                        Intent(
                            this,
                            MusicOnlineActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                    finish()
                }, 1500)
            } else {
                finish()
                exitProcess(0)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        } else {
            updateDataFromSdcard(applicationContext)
            Handler().postDelayed({
                startActivity(
                    Intent(
                        this,
                        MusicOnlineActivity::class.java
                    )
                )
                overridePendingTransition(R.anim.right_to_left, R.anim.right_to_left_out)
                finish()
            }, 1500)
        }
    }

}