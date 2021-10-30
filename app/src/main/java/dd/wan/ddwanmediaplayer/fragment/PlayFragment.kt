package dd.wan.ddwanmediaplayer.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.MyApplication.Companion.list
import dd.wan.ddwanmediaplayer.MyApplication.Companion.listFavorite
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.viewmodel.MyViewModel
import kotlinx.android.synthetic.main.fragment_play.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlayFragment : Fragment(), PlayActivity.OnDataReceivedListener {

    val model by lazy {
        ViewModelProvider(this).get(MyViewModel::class.java)
    }
    var typeOfSong = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_play, container, false)
        model.typeOfSong.observe(requireActivity(), Observer {
            view.type.text =  it
        })

        val mActivity = activity as PlayActivity?
        mActivity!!.setListener(this)
        val bundle = arguments
        val online = bundle!!.getBoolean("online")
        val position = bundle.getInt("position")
        val isFav = bundle.getBoolean("isFav")
        val song = if (online) bundle.getSerializable("song") as Song else Song()
        setUI(view, song, position, online, isFav)
        return view
    }


    private fun setUI(view: View, song: Song, position: Int, online: Boolean, isFav: Boolean) {
        if ((isFav && listFavorite[Constants.position].isOnline) || (online && !isFav)) {
            setUpOn(view, song)
        } else {
            val podcast = if (isFav) listFavorite[position].song
            else list[position]
            setUpOff(view, podcast)
        }
        view.imageView.animation =
            AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
    }

    private fun setUpOff(view: View, song: Podcast) {
        view.name.text = song.title
        view.artists_names.text = song.artist
        view.type.text = song.gener
        if (song.image.isNotEmpty()) {
            try {
                val image = song.image
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

    private fun setUpOn(view: View, song: Song) {
        view.name.text = song.name
        view.artists_names.text = song.artists_names
        GlobalScope.launch {  model.getTypeOfSong(song.id) }
        val linkImg = song.thumbnail.removeRange(34, 48)
        Glide.with(this).load(linkImg).into(view.imageView)
    }



    override fun onDataReceived(song: Song, position: Int, online: Boolean, isFav: Boolean) {
        setUI(requireView(), song, position, online, isFav)
    }

}

