package dd.wan.ddwanmediaplayer.model.songinfo

data class SongInfo(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)