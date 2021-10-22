package dd.wan.ddwanmediaplayer.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.CallAPI
import dd.wan.ddwanmediaplayer.`interface`.CallAPI.Companion.callSearchSong
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.adapter.RecyclerSearch
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.config.Constants.Companion.listSong
import dd.wan.ddwanmediaplayer.model.search.Search
import dd.wan.ddwanmediaplayer.model.top.Music
import dd.wan.ddwanmediaplayer.model.top.Song
import kotlinx.android.synthetic.main.activity_music_online.*
import kotlinx.android.synthetic.main.fragment_online.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OnlineFragment : Fragment() {


    var list = ArrayList<dd.wan.ddwanmediaplayer.model.search.Song>()
    lateinit var adapter: RecyclerMusicAdapter
    lateinit var adapter1: RecyclerSearch
    lateinit var dataTrans: DataTransmission
    var handle = Handler()
    var runnable = Runnable {
        search()
    }
    var query = ""


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
            dataTrans.ChangeData(check1 = true,
                online1 = true,
                activity1 = true,
                currentTime1 = 0,
                position1 = 0,
                isFavorite1 = 0,
                song = listSong[it])
            view.progressBar.visibility = View.VISIBLE
            context?.let { it1 ->
                Constants.getRecommendSong(true, startSer = true, MyApplication.ACTION_PLAY_SONG,
                    it1)
            }
        }
        Log.d("hihihihihihihi",listSong.size.toString())
        if(listSong.size==0)
            getData(view)
        else
            view.progressBar.visibility = View.GONE
        view.listMusicOnline.adapter = adapter
        view.listMusicOnline.layoutManager = LinearLayoutManager(context)

        adapter1 = RecyclerSearch(list)
        adapter1.setCallback {
            val song = Song()
            song.duration = list[it].duration.toInt()
            song.id = list[it].id
            song.thumbnail = "https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${list[it].thumb}"
            song.artists_names = if (list[it].artist != null)
                list[it].artist
            else
                list[it].artists
            song.name = list[it].name
            dataTrans.ChangeData(check1 = true,
                online1 = true,
                activity1 = false,
                currentTime1 = 0,
                position1 = 0,
                isFavorite1 = 0,
                song = song)
            view.progressBar.visibility = View.VISIBLE
            if (Constants.isNetworkConnected(requireContext())) {
                context?.let { it1 ->
                    Constants.getRecommendSong(true,
                        startSer = true,
                        MyApplication.ACTION_PLAY_SONG,
                        it1)
                }
            } else {
                Toast.makeText(context,"Không thể kết nối internet !!!",Toast.LENGTH_LONG).show()
            }

        }
        view.listSearch.adapter = adapter1
        view.listSearch.layoutManager = LinearLayoutManager(context)


        view.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                query = view.searchView.text.toString()
                view.listMusicOnline.visibility = View.GONE
                view?.progressBar?.visibility = View.VISIBLE
                handle.removeCallbacks(runnable)
                handle.postDelayed(runnable, 1000)
            }
        })

        return view
    }

    private fun getData(view: View) {
        if (Constants.isNetworkConnected(requireContext())) {
            val retrofitData = CallAPI.callApi.getTopMusic(0, 0, 0, "song", -1)
            retrofitData.enqueue(object : Callback<Music> {
                override fun onResponse(call: Call<Music>, response: Response<Music>) {
                    val responseBody = response.body()!!
                    listSong.addAll(responseBody.data.song)
                    view.progressBar?.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<Music>, t: Throwable) {
                }
            })
        } else {
            view.progressBar?.visibility = View.GONE
            Toast.makeText(context, "Không thể kết nối internet !!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun search() {
        Log.d("checkList query", query)
        if (query == "") {
            requireView().listSearch.visibility = View.GONE
            requireView().listMusicOnline.visibility = View.VISIBLE
            view?.progressBar?.visibility = View.GONE
        } else {
            requireView().listSearch.visibility = View.VISIBLE
            val retrofit = callSearchSong.searchSong("song", "500", query)
            retrofit.enqueue(object : Callback<Search> {
                override fun onResponse(call: Call<Search>, response: Response<Search>) {
                    val responseBody = response.body()!!
                    list.clear()
                    if (responseBody.data.isNotEmpty())
                        list.addAll(responseBody.data[0].song)
                    adapter1.notifyDataSetChanged()
                    view?.progressBar?.visibility = View.GONE
                }

                override fun onFailure(call: Call<Search>, t: Throwable) {
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        handle.removeCallbacks(runnable)
    }
}