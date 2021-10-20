package dd.wan.ddwanmediaplayer.adapter

import android.content.Context
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

class RecyclerSearch(var listSong: ArrayList<dd.wan.ddwanmediaplayer.model.search.Song>) :
    RecyclerView.Adapter<RecyclerSearch.ViewHolder>() {

    lateinit var context: Context
    lateinit var itemClick: (position: Int) -> Unit
    fun setCallback(click: (position: Int) -> Unit) {
        itemClick = click
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerSearch.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerSearch.ViewHolder, position: Int) {
        holder.setData()
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var artistsNames: TextView = itemView.findViewById(R.id.artists_names)
        var layout: CardView = itemView.findViewById(R.id.layout)
        var time: TextView = itemView.findViewById(R.id.time)
        var imagePodcast: ImageView = itemView.findViewById(R.id.imagePodcast)

        fun setData() {
            time.text = Constants.sdf.format(listSong[adapterPosition].duration.toInt() * 1000)
            name.text = listSong[adapterPosition].name
            artistsNames.text =
                if (listSong[adapterPosition].artist != null)
                    listSong[adapterPosition].artist
                else
                    listSong[adapterPosition].artists
            Glide.with(context)
                .load("https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${listSong[adapterPosition].thumb}")
                .centerInside()
                .into(imagePodcast)
            layout.setOnClickListener { itemClick.invoke(adapterPosition) }

        }
    }
}