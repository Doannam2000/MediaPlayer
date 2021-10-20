package dd.wan.ddwanmediaplayer.`interface`

import dd.wan.ddwanmediaplayer.model.top.Song

interface DataFragToAct {
    fun sendData(song: Song,position: Int,online:Boolean,isFav:Boolean)
}