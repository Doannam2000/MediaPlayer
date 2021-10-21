package dd.wan.ddwanmediaplayer.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataFragToAct
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.adapter.RecyclerFavoriteMusic
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.top.Song
import kotlinx.android.synthetic.main.fragment_recommend.view.*


class RecommendFragment : Fragment() {

    lateinit var dataFragToAct: DataFragToAct
    lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>


    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataFragToAct = context as PlayActivity
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized)
            adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)
        val bundle = arguments
        val isFav = bundle!!.getBoolean("isFav")
        adapter = if (isFav) {
            val adapter = RecyclerFavoriteMusic(listFavorite)
            adapter.setCallback {
                if (Constants.isNetworkConnected(requireContext())) {
                    val song = Constants.getFavoriteSong(listFavorite[it])
                    dataFragToAct.sendData(song, it, listFavorite[it].isOnline, isFav)
                } else {
                    Toast.makeText(context, "Không thể kết nối internet", Toast.LENGTH_LONG).show()
                }
            }
            adapter
        } else {
            val online = bundle.getBoolean("online")
            val adapter = if (online) {
                val list = bundle.getSerializable("listRecommend") as ArrayList<Song>
                val adapter = RecyclerMusicAdapter(list)
                adapter.setCallback {
                    dataFragToAct.sendData(list[it], it, online, isFav)
                }
                adapter
            } else {
                val adapter = RecyclerAdapter(list)
                adapter.setCallback {
                    dataFragToAct.sendData(Song(), it, online, isFav)
                }
                adapter
            }
            adapter
        }

        view.recyclerRecommend.adapter = adapter
        view.recyclerRecommend.layoutManager = LinearLayoutManager(context)
        return view
    }

}