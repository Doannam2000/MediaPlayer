package dd.wan.ddwanmediaplayer.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.CallAPI.Companion.callApi
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.model.songinfo.SongInfo
import dd.wan.ddwanmediaplayer.model.top.Song
import kotlinx.android.synthetic.main.fragment_play.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlayFragment : Fragment(), PlayActivity.OnDataReceivedListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_play, container, false)
        val mActivity = activity as PlayActivity?
        mActivity!!.setListener(this)
        val bundle = arguments
        val online = bundle!!.getBoolean("online")
        val position = bundle.getInt("position")
        val song = if (online) bundle.getSerializable("song") as Song else Song()
        setUI(view, song, position, online)
        return view
    }

    override fun onDataReceived(song: Song, position: Int, online: Boolean) {
        setUI(requireView(), song, position, online)
    }

    private fun setUI(view: View, song: Song, position: Int, online: Boolean) {
        if (online) {
            view.name.text = song.name
            view.artists_names.text = song.artists_names
            GlobalScope.launch { getTypeMusic(song.id,view) }
            val linkImg = song.thumbnail.removeRange(34, 48)
            Glide.with(this).load(linkImg).into(view.imageView)
        } else {
            view.name.text = list[position].title
            view.artists_names.text = list[position].artist
            view.type.text = " "
            if (list[position].image.isNotEmpty()) {
                try {
                    val image = list[position].image
                    view.imageView.setImageBitmap(BitmapFactory.decodeByteArray(image,
                        0,
                        image.size))
                } catch (e: Exception) {
                    view.imageView.setImageResource(R.drawable.music_icon)
                }
            } else {
                view.imageView.setImageResource(R.drawable.music_icon)
            }
        }
        view.imageView.animation =
            AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
    }

    private fun getTypeMusic(id:String, view: View){
        val call = callApi.getInfo("audio",id)
        call.enqueue(object :Callback<SongInfo>{
            override fun onResponse(call: Call<SongInfo>, response: Response<SongInfo>) {
                val responseBody = response.body()!!
                view.type.text = responseBody.data.genres[1].name
            }

            override fun onFailure(call: Call<SongInfo>, t: Throwable) {

            }
        })
    }
}

