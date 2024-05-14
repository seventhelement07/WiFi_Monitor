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
import com.seventhelement.wifimonitor.data


class Adapte3(
    private val list: MutableList<String>,
) : RecyclerView.Adapter<Adapte3.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.ipaddress)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_4, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        holder.title.text=current

    }
    fun addDevice(device: String) {
        list.add(device)
        notifyItemInserted(list.size - 1)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)

    }
}