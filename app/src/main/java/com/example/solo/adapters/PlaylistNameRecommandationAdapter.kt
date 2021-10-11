package com.example.solo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.solo.R


class PlaylistNameRecommandationAdapter(var playlistNames: List<String>,var editText: EditText?) : RecyclerView.Adapter<PlaylistNameRecommandationAdapter.PlaylistNames>() {

    var filteredPlaylistNames= mutableListOf<String>()
    inner class PlaylistNames(Itemview: View) : RecyclerView.ViewHolder(Itemview) {
        var playlistTitle: TextView = Itemview.findViewById<TextView>(R.id.playlistNameRec)
        var playlistItem: CardView = Itemview.findViewById<CardView>(R.id.playlistItemRec)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistNames {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.playlist_name_recommendation_layout, parent, false)
        return PlaylistNames(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistNames, position: Int) {
        Log.i("testnames","recommandation adapter worked")
        holder.playlistTitle.text = playlistNames[position]

        if (playlistNames.isEmpty())holder.playlistItem.visibility=View.GONE
        holder.playlistItem.setOnClickListener {
            selectExistingPlaylist(position)
        }
    }

    private fun selectExistingPlaylist(position: Int) {
        editText!!.setText(playlistNames[position])
    }

    override fun getItemCount(): Int {
        return playlistNames.size
    }
}