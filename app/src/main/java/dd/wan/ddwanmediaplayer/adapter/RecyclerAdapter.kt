package dd.wan.ddwanmediaplayer.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.model.Podcast

class RecyclerAdapter(var list: ArrayList<Podcast>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    lateinit var itemClick: (position: Int) -> Unit
    fun setCallback(click: (position: Int) -> Unit) {
        itemClick = click
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.setData()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var layout: CardView = itemView.findViewById(R.id.layout)
        var imagePodcast: ImageView = itemView.findViewById(R.id.imagePodcast)
        fun setData() {
            name.text = list[adapterPosition].title
            if (list[adapterPosition].image.isNotEmpty()) {
                var image = list[adapterPosition].image
                imagePodcast.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image!!.size))
            }

        }

        init {
            layout.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }
    }
}