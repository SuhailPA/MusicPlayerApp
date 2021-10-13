package com.suhail_music_app.solo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.modelclass.musicServices

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlaylistsRecyclerAdapter(var playlistNames: MutableList<String>, var onLongClick: ((Int, MutableList<String>) -> Unit)?) : RecyclerView.Adapter<PlaylistsRecyclerAdapter.PlaylistFiles>() {


    inner class PlaylistFiles(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var playlistItem: CardView = ItemView.findViewById(R.id.playListItem)
        var playlistThumbnail: ImageView = ItemView.findViewById(R.id.playlistThumbnail)
        var playlistName: TextView = ItemView.findViewById(R.id.playlistName)

        init {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistFiles {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_in_favourite, parent, false)


        return PlaylistFiles(itemView)
    }


    fun updateAdapter(mDataList: MutableList<String>) {
        this.playlistNames = mDataList
        Log.i("AdapterFunction","Test")
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: PlaylistFiles, position: Int) {
        holder.playlistName.text = playlistNames[position]
        Log.i("PlaylistFileNameSize", playlistNames.size.toString())

        holder.playlistItem.setOnLongClickListener{
            onLongClick?.let { it1 -> it1(position, playlistNames) }
//          alertBox(position)
            return@setOnLongClickListener true
        }
        holder.playlistItem.setOnClickListener {

            GlobalScope.launch(Dispatchers.IO){
                musicServices?.get().let { it?.getPlaylistSongs(playlistNames[position]) }
                withContext(Dispatchers.Main){
                    Log.i("checkerror",playlistNames[position])
                    musicServices?.get().let { it?.navigationFromMainFragmentToPlaylistFragment(playlistNames[position]) }
                }
            }


        }
    }

    override fun getItemCount(): Int {
        return playlistNames.size
    }
}