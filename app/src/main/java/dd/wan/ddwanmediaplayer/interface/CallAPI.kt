package dd.wan.ddwanmediaplayer.`interface`

import android.os.Build
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.recommend.RecommendMusic
import dd.wan.ddwanmediaplayer.model.search.Search
import dd.wan.ddwanmediaplayer.model.songinfo.SongInfo
import dd.wan.ddwanmediaplayer.model.top.Music
import okhttp3.ConnectionSpec
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient


interface CallAPI {


    @GET("xhr/chart-realtime")
    fun getTopMusic(
        @Query("songId") songId: Int,
        @Query("videoId") videoId: Int,
        @Query("albumId") albumId: Int,
        @Query("chart") chart: String,
        @Query("time") time: Int,
    ): Call<Music>

    @GET("xhr/recommend")
    fun getRecommendSong(
        @Query("type") type: String,
        @Query("id") id: String,
    ): Call<RecommendMusic>

    @GET("xhr/media/get-info")
    fun getInfo(
        @Query("type") type: String,
        @Query("id") id: String,
    ): Call<SongInfo>

    @GET("/complete")
    fun searchSong(
        @Query("type") type: String,
        @Query("num") num: String,
        @Query("query") query: String,
    ): Call<Search>
}