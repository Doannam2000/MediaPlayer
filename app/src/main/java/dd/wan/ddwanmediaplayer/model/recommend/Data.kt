package dd.wan.ddwanmediaplayer.model.recommend

import dd.wan.ddwanmediaplayer.model.top.Song
import java.io.Serializable

data class Data(
    val image_url: String,
    val items: List<Song>,
    val total: Int
): Serializable