package dd.wan.ddwanmediaplayer.model.top

import java.io.Serializable

data class Artist(
    val link: String,
    val name: String
): Serializable{
    constructor() :this("","")
}