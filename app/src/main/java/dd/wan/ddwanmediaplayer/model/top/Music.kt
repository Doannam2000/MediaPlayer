package dd.wan.ddwanmediaplayer.model.top

data class Music(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)