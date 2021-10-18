package dd.wan.ddwanmediaplayer.model.top

import java.io.Serializable

data class ArtistX(
    val cover: String,
    val id: String,
    val link: String,
    val name: String,
    val thumbnail: String
): Serializable {
    constructor() :this("","","","","")
}