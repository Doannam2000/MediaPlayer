package dd.wan.ddwanmediaplayer.`interface`

import dd.wan.ddwanmediaplayer.model.top.Song

interface DataTransmission {

    fun ChangeData(
        check1: Boolean,
        online1: Boolean,
        activity1: Boolean,
        currentTime1: Int,
        position1: Int,
        isFavorite1:Int,
        song:Song
    )
}