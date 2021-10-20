package dd.wan.ddwanmediaplayer.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataFragToAct
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.model.top.Song
import kotlinx.android.synthetic.main.fragment_recommend.view.*


class RecommendFragment : Fragment() {

    lateinit var dataFragToAct: DataFragToAct

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataFragToAct = context as PlayActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)
        val bundle = arguments
        val online = bundle!!.getBoolean("online")
        val adapter = if(online){
            val list = bundle.getSerializable("listRecommend") as ArrayList<Song>
            val adapter = RecyclerMusicAdapter(list)
            adapter.setCallback {
                dataFragToAct.sendData(list[it],it,online)
            }
            adapter
        }else{
            val adapter = RecyclerAdapter(list)
            adapter.setCallback {
                dataFragToAct.sendData(Song(),it,online)
            }
            adapter
        }
        view.recyclerRecommend.adapter = adapter
        view.recyclerRecommend.layoutManager = LinearLayoutManager(context)
        return  view
    }


}