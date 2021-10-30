package dd.wan.ddwanmediaplayer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.repository.Repository
import dd.wan.ddwanmediaplayer.sql.SQLHelper

class MyViewModel:ViewModel() {

    var listMusic = MutableLiveData<ArrayList<Song>>()
    var listTopMusic = MutableLiveData<ArrayList<Song>>()
    var listSearchMusic = MutableLiveData<ArrayList<dd.wan.ddwanmediaplayer.model.search.Song>>()
    var typeOfSong = MutableLiveData<String>()
    var listOffline = MutableLiveData<ArrayList<Podcast>>()
    var listFav = MutableLiveData<ArrayList<FavoriteSong>>()
    var repository = Repository()

    fun getRecommendMusic(){
        repository.getRecommendSong(listMusic)
    }

    fun getTopMusic(){
        repository.getListTop(listTopMusic)
    }

    fun getSearchMusic(query:String){
        repository.getListSearch(listSearchMusic,query)
    }

    fun getTypeOfSong(id:String){
        repository.getType(typeOfSong,id)
    }

    fun getPodcast(context:Context){
        repository.loadSong(context,listOffline)
    }

    fun getFavoriteSQL(context: Context){
        listFav.value = SQLHelper(context).getAll()
    }
}