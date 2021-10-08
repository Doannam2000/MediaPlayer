package dd.wan.ddwanmediaplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle

class Broadcast: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        var action = p1!!.extras as Bundle
        var intent = Intent(p0,MyService::class.java)
        intent.putExtras(action)
        p0!!.startService(intent)
    }
}