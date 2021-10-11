package dd.wan.ddwanmediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlin.system.exitProcess

class HelloActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        requestPermission()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Handler().postDelayed({
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left_out)
                    finish()
                }, 2000)
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
            Handler().postDelayed({
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
                overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left_out)
                finish()
            }, 2000)
        }
    }
}