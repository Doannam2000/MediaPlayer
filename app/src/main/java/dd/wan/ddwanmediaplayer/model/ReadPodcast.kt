package dd.wan.ddwanmediaplayer.model

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log

class ReadPodcast(var context: Context) {
    @SuppressLint("Range")
    fun loadSong(): ArrayList<Podcast> {
        var list = ArrayList<Podcast>()
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var rs = context.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID
            ),
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )
        if (rs != null) {
            while (rs!!.moveToNext()) {
                var uri = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DATA))
                var title = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.TITLE))
                var artist = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                var media = MediaMetadataRetriever()
                media.setDataSource(uri)
                var duration = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) as String

                var bitmap: ByteArray? = media.embeddedPicture
                if (bitmap == null) {
                    bitmap = byteArrayOf()
                }
                list.add(Podcast(uri, title, artist, bitmap,duration.toInt()))
            }
        }
        return list
    }
}