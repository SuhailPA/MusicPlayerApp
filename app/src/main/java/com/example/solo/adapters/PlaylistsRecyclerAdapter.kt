package com.example.solo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.solo.R
import com.example.solo.modelclass.musicServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistsRecyclerAdapter(var playlistNames: MutableList<String>, var onLongClick: ((Int, MutableList<String>) -> Unit)?) : RecyclerView.Adapter<PlaylistsRecyclerAdapter.PlaylistFiles>() {


    inner class PlaylistFiles(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var playlistItem: CardView = ItemView.findViewById(R.id.playListItem)
        var playlistThumbnail: ImageView = ItemView.findViewById(R.id.playlistThumbnail)
        var playlistName: TextView = ItemView.findViewById(R.id.playlistName)

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
                musicServices!!.getPlaylistSongs(playlistNames[position])
                withContext(Dispatchers.Main){
                    musicServices!!.navigationFromMainFragmentToPlaylistFragment(playlistNames[position])
                }
            }


        }


    }

//    private fun alertBox(position: Int){
//        var alertDialog=AlertDialog.Builder(view!!.context)
//        alertDialog.setTitle("Delete")
//        alertDialog.setMessage("Are you sure to delete the playlist")
//        alertDialog.setPositiveButton("Yes",DialogInterface.OnClickListener { dialog, which ->
//            deletePlaylist(position)
//        })
//
//        alertDialog.setNegativeButton("No",DialogInterface.OnClickListener { dialog, which ->
//            dialog.dismiss()
//        })
//        alertDialog.show()
//    }
//
//    private fun deletePlaylist(position:Int){
//        GlobalScope.launch(Dispatchers.IO){
//            musicServices!!.deleteWholePlaylist(playlistNames[position])
//            withContext(Dispatchers.Main){
//
//                playlistFiles.remove(playlistNames[position])
//                this@PlaylistsRecyclerAdapter.notifyDataSetChanged()
//                Toast.makeText(view!!.context,"Successfully deleted",Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
    override fun getItemCount(): Int {
        return playlistNames.size
    }
}