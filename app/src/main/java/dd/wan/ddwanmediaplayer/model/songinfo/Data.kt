package dd.wan.ddwanmediaplayer.model.songinfo

data class Data(
    val artists: List<Artist>,
    val composers: List<Composer>,
    val contentOwner: ContentOwner,
    val genres: List<Genre>,
    val info: Info
)