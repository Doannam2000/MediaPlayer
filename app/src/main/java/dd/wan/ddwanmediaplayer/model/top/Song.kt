package dd.wan.ddwanmediaplayer.model.top

import java.io.Serializable

data class Song(
    var album: Album,
    var artist: ArtistX,
    var artists: List<ArtistXX>,
    var artists_names: String,
    var code: String,
    var content_owner: Int,
    var duration: Int,
    var id: String,
    var isWorldWide: Boolean,
    var isoffical: Boolean,
    var link: String,
    var lyric: String,
    var name: String,
    var order: String,
    var performer: String,
    var playlist_id: String,
    var position: Int,
    var rank_num: String,
    var rank_status: String,
    var thumbnail: String,
    var title: String,
    var total: Int,
    var type: String,
) : Serializable {
    constructor() : this(
        Album(),
        ArtistX(),
        emptyList(),
        "",
        "",
        -1,
        -1,
        "",
        false,
        false,
        "",
        "",
        "",
        "",
        "",
        "",
        -1,
        "",
        "",
        "", "", -1, ""
    )

}