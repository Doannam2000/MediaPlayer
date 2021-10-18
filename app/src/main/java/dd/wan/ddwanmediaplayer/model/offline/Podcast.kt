package dd.wan.ddwanmediaplayer.model.offline

import java.io.Serializable

class Podcast(var uri:String,var title:String,var artist :String,var image:ByteArray,var duration:Int):Serializable {
}