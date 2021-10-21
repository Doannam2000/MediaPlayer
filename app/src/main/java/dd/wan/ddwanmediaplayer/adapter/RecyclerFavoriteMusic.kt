package dd.wan.ddwanmediaplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.FavoriteSong

class RecyclerFavoriteMusic(var list: ArrayList<FavoriteSong>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context: Context
    lateinit var itemClick: (position: Int) -> Unit
    fun setCallback(click: (position: Int) -> Unit) {
        itemClick = click
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerFavoriteMusic.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var layout: CardView = itemView.findViewById(R.id.layout)
        var artists_names: TextView = itemView.findViewById(R.id.artists_names)
        var time: TextView = itemView.findViewById(R.id.time)
        var imagePodcast: ImageView = itemView.findViewById(R.id.imagePodcast)
        @SuppressLint("CheckResult")
        fun setData() {
            artists_names.text = list[adapterPosition].song.artist
            name.text = list[adapterPosition].song.title
            if (list[adapterPosition].isOnline) {
                time.text = Constants.sdf.format(list[adapterPosition].song.duration * 1000)
                Glide.with(context).load(list[adapterPosition].thumbnail).into(imagePodcast)
            } else {
                time.text = Constants.sdf.format(list[adapterPosition].song.duration)
                if (list[adapterPosition].song.image.isNotEmpty()) {
                    try {
                        var image = list[adapterPosition].song.image
                        imagePodcast.setImageBitmap(BitmapFactory.decodeByteArray(image,
                            0,
                            image.size))
                    } catch (e: Exception) {
                    }
                }
            }
        }
        init {
            layout.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.setData()
        }
    }
}