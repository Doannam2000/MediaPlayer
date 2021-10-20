package dd.wan.ddwanmediaplayer.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.CallAPI
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.top.Music
import dd.wan.ddwanmediaplayer.model.top.Song
import kotlinx.android.synthetic.main.activity_music_online.*
import kotlinx.android.synthetic.main.fragment_online.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OnlineFragment : Fragment() {

    var listSong = ArrayList<Song>()
    lateinit var adapter: RecyclerMusicAdapter
    lateinit var dataTrans: DataTransmission

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataTrans = context as DataTransmission
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_online, container, false)
        adapter = RecyclerMusicAdapter(listSong)
        adapter.setCallback {
            view.progressBar.visibility = View.VISIBLE
            context?.let { it1 ->
                Constants.getRecommendSong(true, startSer = true, MyApplication.ACTION_PLAY_SONG,
                    it1)
            }
            dataTrans.ChangeData(check1 = true,
                online1 = true,
                activity1 = false,
                currentTime1 = 0,
                position1 = 0,
                song = listSong[it])
        }
        getData()
        view.listMusicOnline.adapter = adapter
        view.listMusicOnline.layoutManager = LinearLayoutManager(context)
        return view
    }

    private fun getData() {
        val retrofitData = CallAPI.callApi.getTopMusic(0, 0, 0, "song", -1)
        retrofitData.enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                val responseBody = response.body()!!
                listSong.addAll(responseBody.data.song)
                view?.progressBar?.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Music>, t: Throwable) {
                Log.d("dulieu", "Đếch call được")
            }
        })
    }

}