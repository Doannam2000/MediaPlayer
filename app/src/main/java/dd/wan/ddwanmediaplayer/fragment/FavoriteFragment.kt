package dd.wan.ddwanmediaplayer.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerFavoriteMusic
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.fragment_favorite.searchView
import kotlinx.android.synthetic.main.fragment_favorite.view.*


class FavoriteFragment : Fragment() {

    lateinit var dataTrans: DataTransmission
    lateinit var adapter:RecyclerFavoriteMusic
    var listP = ArrayList<FavoriteSong>()
    val handle = Handler()
    val run = Runnable {
        val text = searchView.text
        listFavorite.clear()
        for (item in listP) {
            if (item.song.title.uppercase().contains(text.toString().uppercase())) {
                listFavorite.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataTrans = context as DataTransmission
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        var view = inflater.inflate(R.layout.fragment_favorite, container, false)
        view.list_Favorite.layoutManager = LinearLayoutManager(context)
        adapter= RecyclerFavoriteMusic(listFavorite)
        adapter.setCallback {
            val favoriteMusic = listFavorite[it]
            var song = Song()
            if (favoriteMusic.isOnline) {
                song.name = favoriteMusic.song.title
                song.id = favoriteMusic.song.uri
                song.artists_names = favoriteMusic.song.artist
                song.thumbnail = favoriteMusic.thumbnail
                song.duration = favoriteMusic.song.duration
            }
            dataTrans.ChangeData(
                check1 = true,
                online1 = favoriteMusic.isOnline,
                activity1 = true,
                currentTime1 = 0,
                position1 = it,
                isFavorite1 = 1,
                song
            )
        }
        view.list_Favorite.adapter = adapter
        listP.addAll(listFavorite)
        view.searchView.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                handle.removeCallbacks(run)
                handle.postDelayed(run,1000)
            }
        })
        return view
    }
}