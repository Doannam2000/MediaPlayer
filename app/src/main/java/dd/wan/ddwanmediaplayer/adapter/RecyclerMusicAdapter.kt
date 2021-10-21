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
import dd.wan.ddwanmediaplayer.config.Constants.Companion.sdf
import dd.wan.ddwanmediaplayer.model.top.Song

class RecyclerMusicAdapter(var listSong:ArrayList<Song>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context:Context
    lateinit var itemClick: (position: Int) -> Unit
    fun setCallback(click: (position: Int) -> Unit) {
        itemClick = click
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerMusicAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item,parent,false)
        context = parent.context
        return ViewHolder(view)
    }

//    override fun onBindViewHolder(holder: RecyclerMusicAdapter.ViewHolder, position: Int) {
//        holder.setData()
//    }

    override fun getItemCount(): Int {
        return listSong.size
    }
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var name: TextView = itemView.findViewById(R.id.name)
        var artists_names: TextView = itemView.findViewById(R.id.artists_names)
        var layout: CardView = itemView.findViewById(R.id.layout)
        var time:TextView = itemView.findViewById(R.id.time)
        var imagePodcast: ImageView = itemView.findViewById(R.id.imagePodcast)

        fun setData() {
            time.text = sdf.format(listSong[adapterPosition].duration*1000)
            name.text = listSong[adapterPosition].name
            artists_names.text = listSong[adapterPosition].artists_names
            Glide.with(context)
                .load(listSong[adapterPosition].thumbnail)
                .centerInside()
                .into(imagePodcast)
            layout.setOnClickListener { itemClick.invoke(adapterPosition) }

        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ViewHolder)
            holder.setData()
    }
}