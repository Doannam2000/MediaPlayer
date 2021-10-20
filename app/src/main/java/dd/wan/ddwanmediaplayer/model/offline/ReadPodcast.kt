package dd.wan.ddwanmediaplayer.model.offline

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore

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
                MediaStore.Audio.Media.DURATION
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
                var duration =
                    rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DURATION)) as String
                var media = MediaMetadataRetriever()
                var gener = ""
                try {
                    media.setDataSource(uri)
                    gener = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE).toString()
                } catch (e: Exception) {
                }
                var bitmap: ByteArray = try {
                    if (media.embeddedPicture != null) {
                        media.embeddedPicture!!
                    } else
                        byteArrayOf()
                } catch (e: Exception) {
                    byteArrayOf()
                }
                list.add(Podcast(uri, title, artist, bitmap, duration.toInt(),gener))
            }
        }
        return list
    }
}