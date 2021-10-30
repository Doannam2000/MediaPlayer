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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listRecommendMusic
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.activities.MusicOnlineActivity
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerMusicAdapter
import dd.wan.ddwanmediaplayer.adapter.RecyclerSearch
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.config.Constants.Companion.listSong
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import dd.wan.ddwanmediaplayer.viewmodel.MyViewModel
import kotlinx.android.synthetic.main.fragment_online.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val model by lazy {
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataTrans = context as DataTransmission
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_online, container, false)
        view.listMusicOnline.layoutManager = LinearLayoutManager(context)
        view.listSearch.layoutManager = LinearLayoutManager(context)


        model.listTopMusic.observe(requireActivity(), Observer {
            listSong = it
            setUpAdapter()
            view.listMusicOnline.adapter = adapter
        })

        model.listSearchMusic.observe(requireActivity(), Observer { data ->
            list = data
            setUpAdapter1()
            view.listSearch.adapter = adapter1
        })

        if (listSong.size == 0) {
            getData(view)
        } else {
            view.progressBar.visibility = View.GONE
        }
        setUpAdapter()
        view.listMusicOnline.adapter = adapter
        setUpAdapter1()
        view.listSearch.adapter = adapter1

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
            model.getTopMusic()
            view.progressBar?.visibility = View.GONE
        } else {
            view.progressBar?.visibility = View.GONE
            Toast.makeText(context, "Không thể kết nối internet !!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun search() {
        if (Constants.isNetworkConnected(requireContext())) {
            if (query == "") {
                requireView().listSearch.visibility = View.GONE
                requireView().listMusicOnline.visibility = View.VISIBLE
                requireView().progressBar?.visibility = View.GONE
            } else {
                model.getSearchMusic(query)
                requireView().listSearch.visibility = View.VISIBLE
                requireView().progressBar?.visibility = View.GONE
            }
        } else {
            Toast.makeText(context, "Không thể kết nối internet !!!", Toast.LENGTH_LONG).show()
        }
    }


    override fun onPause() {
        super.onPause()
        handle.removeCallbacks(runnable)
    }

    private fun setUpAdapter() {
        adapter = RecyclerMusicAdapter(listSong)
        adapter.setCallback {
            dataTrans.ChangeData(check1 = true,
                online1 = true,
                activity1 = true,
                currentTime1 = 0,
                position1 = 0,
                isFavorite1 = 0,
                song = listSong[it])
            requireView().progressBar.visibility = View.VISIBLE
        }
    }

    private fun setUpAdapter1() {
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
            requireView().progressBar.visibility = View.VISIBLE
        }

    }
}