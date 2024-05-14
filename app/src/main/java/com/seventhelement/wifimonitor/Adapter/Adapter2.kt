package com.seventhelement.wifimonitor.Adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seventhelement.wifimonitor.R


class Adapter2(
    private val list: ArrayList<String>,val listener: OnItemSelectedListener
) : RecyclerView.Adapter<Adapter2.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.name)
       val ll:LinearLayout=itemView.findViewById(R.id.ll_layout)
        val image: ImageView = itemView.findViewById(R.id.imageView2)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        holder.title.text=current
        holder.image.setImageResource(R.drawable.wifi)
        holder.ll.setOnClickListener {
            listener.onItemSelected(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)

    }
}