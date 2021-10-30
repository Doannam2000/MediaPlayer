package dd.wan.ddwanmediaplayer.repository

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import dd.wan.ddwanmediaplayer.`interface`.CallAPI
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.recommend.RecommendMusic
import dd.wan.ddwanmediaplayer.model.search.Search
import dd.wan.ddwanmediaplayer.model.songinfo.SongInfo
import dd.wan.ddwanmediaplayer.model.top.Music
import dd.wan.ddwanmediaplayer.model.top.Song
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Repository {
    companion object {

        val callApi: CallAPI = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL)
            .build()
            .create(CallAPI::class.java)

        val callSearchSong: CallAPI = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL_SEARCH)
            .build()
            .create(CallAPI::class.java)
    }

    fun getRecommendSong(liveData: MutableLiveData<ArrayList<Song>>) {
        val retrofit = callApi.getRecommendSong("audio", Constants.song.id)
        retrofit.enqueue(object : Callback<RecommendMusic> {
            override fun onResponse(
                call: Call<RecommendMusic>,
                response: Response<RecommendMusic>,
            ) {
                val responseBody = response.body()!!
                liveData.value = responseBody.data.items as ArrayList<Song>
            }

            override fun onFailure(call: Call<RecommendMusic>, t: Throwable) {
            }
        })
    }

    fun getListTop(liveData: MutableLiveData<ArrayList<Song>>) {
        val retrofitData = callApi.getTopMusic(0, 0, 0, "song", -1)
        retrofitData.enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                val responseBody = response.body()!!
                liveData.value = responseBody.data.song as ArrayList<Song>
            }
            override fun onFailure(call: Call<Music>, t: Throwable) {
            }
        })
    }

    fun getListSearch(liveData: MutableLiveData<ArrayList<dd.wan.ddwanmediaplayer.model.search.Song>>,query:String){
        val retrofit = callSearchSong.searchSong("song", "500", query)
        retrofit.enqueue(object : Callback<Search> {
            override fun onResponse(call: Call<Search>, response: Response<Search>) {
                val responseBody = response.body()!!
                if (responseBody.data.isNotEmpty())
                    liveData.value = responseBody.data[0].song as ArrayList<dd.wan.ddwanmediaplayer.model.search.Song>
            }

            override fun onFailure(call: Call<Search>, t: Throwable) {
            }
        })
    }


    fun getType(liveData: MutableLiveData<String>,id: String){
        val call = callApi.getInfo("audio", id)
        call.enqueue(object : Callback<SongInfo> {
            override fun onResponse(call: Call<SongInfo>, response: Response<SongInfo>) {
                val responseBody = response.body()!!
                liveData.value = responseBody.data.genres[1].name
            }

            override fun onFailure(call: Call<SongInfo>, t: Throwable) {
            }
        })
    }


    @SuppressLint("Range")
    fun loadSong(context:Context,liveData: MutableLiveData<ArrayList<Podcast>>){
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
                try {
                    var uri = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DATA))
                    var title = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    var artist =
                        rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    var duration =
                        rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DURATION)) as String
                    var media = MediaMetadataRetriever()
                    var gener = " "
                    try {
                        media.setDataSource(uri)
                        gener = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                            .toString()
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
                    list.add(Podcast(uri, title, artist, bitmap, duration.toInt(), gener))
                } catch (e: Exception) {

                }
            }
        }
        liveData.value = list
    }

}