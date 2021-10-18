package dd.wan.ddwanmediaplayer.model.recommend

import java.io.Serializable


data class RecommendMusic(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
): Serializable