package dd.wan.ddwanmediaplayer.model

import android.graphics.Bitmap
import java.io.Serializable
import java.time.Duration

class Podcast(var uri:String,var title:String,var artist :String,var image:ByteArray,var duration:Int):Serializable {
}