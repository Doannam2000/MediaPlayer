package dd.wan.ddwanmediaplayer.model.top

data class SongHis(
    val `data`: DataX,
    val from: Long,
    val interval: Int,
    val max_score: Double,
    val min_score: Int,
    val score: Score,
    val total_score: Int
)