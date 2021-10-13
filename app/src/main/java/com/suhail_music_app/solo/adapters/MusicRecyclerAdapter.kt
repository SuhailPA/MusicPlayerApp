package com.suhail_music_app.solo.adapters

//import com.example.solo.fragments.HomeFragmentDirections

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.suhail_music_app.solo.MainActivity
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.interfaces.OnItemClickListner
import com.suhail_music_app.solo.modelclass.*
import kotlinx.coroutines.*


class MusicRecyclerAdapter(
        var musiclist: MutableList<MusicModelClass>,
        var context: FragmentActivity,
        var view: View,
        var value: Int,
        val onClick:(MusicModelClass, Int)->Unit,
        val popUpSelection:(View,Int,MutableList<MusicModelClass>)->Unit
) : RecyclerView.Adapter<MusicRecyclerAdapter.MusicClassholder>() {
    lateinit var mainActivity:MainActivity

//    var playlistNames = mutableListOf<String>()

    

    private lateinit var adapterPF:PlaylistsRecyclerAdapter
    var musicFavDao = musicFavourites.getDatabase(context.application).musicDao()

    var checkPlaylistNames= mutableListOf<String>()

    inner class MusicClassholder(ItemView: View) : RecyclerView.ViewHolder(ItemView), View.OnClickListener {
        val thumbNail: ImageView = ItemView.findViewById(R.id.song_image_thumbnail)
        val songName: TextView = ItemView.findViewById(R.id.song_Name)
        val duration: TextView = ItemView.findViewById(R.id.duration)
        val artist: TextView = ItemView.findViewById(R.id.artistName)
        private val optionsMenu: ImageView=ItemView.findViewById(R.id.optionsMenu)



        init {
            mainActivity=MainActivity()
            ItemView.setOnClickListener(this)
            optionsMenu.setOnClickListener(this)
            if (value==2)optionsMenu.visibility=View.GONE
        }


        var onItemClick: OnItemClickListner? = null

        fun setOnItemClicklistner(itemClicklistner: OnItemClickListner) {
            mainActivity=MainActivity()

            this.onItemClick = itemClicklistner
        }

        override fun onClick(v: View?) {
            when(v!!.id){
                R.id.optionsMenu -> {

                    popUpSelection(v,adapterPosition,musiclist)
//                    showPopup(v, adapterPosition)
                }
                else->this.onItemClick?.onItemClickListner(v, adapterPosition)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicClassholder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.music_item_layout,
            parent,
            false
        )

        mainActivity=MainActivity()

        readPlaylistFiles()
        return MusicClassholder(itemView)
    }


    override fun onBindViewHolder(holder: MusicClassholder, position: Int) {
        mainActivity=MainActivity()
        Log.i("Working","iiadapeter")
        if (value==3){
//            adapterPF= PlaylistsRecyclerAdapter(playlistFiles,view)
        }
        holder.songName.isSelected = true
        holder.artist.isSelected = true
        Log.i("lisstCheck",musiclist[0].songName)
        holder.songName.text = musiclist[position].songName
        holder.duration.text = formatDuration(musiclist[position].duration)
        holder.artist.text = musiclist[position].artistName

        var imagePath = musiclist[position].imagePath
        var imagePathUri = Uri.parse(imagePath)
        if (imagePathUri != null)Glide.with(context).load(imagePathUri).placeholder(R.drawable.music_thumbnail).into(
            holder.thumbNail
        )


        holder.setOnItemClicklistner(object : OnItemClickListner {
            override fun onItemClickListner(view: View, pos: Int) {


                onClick(musiclist[position],position)

                if (value == 1) {
                    musicServices?.get().let { it?.songSelectionList(1) }
//                    var actions: NavDirections =
//                        MainFragmentDirections.actionMainFragmentToMusicFragment(
//                            pos,
//                            musicServices!!.serviceList[pos].songId
//                        )
//                    musicServices!!.serviceList.sortedBy { it.playlistName }
//                    Log.i("SongName", musicServices!!.serviceList[pos].songName)
//                    navController.navigate(actions)
                } else if (value == 2) {
                     musicServices?.get().let { it?.songSelectionList(2) }
//                    var actions: NavDirections =
//                        SongListsFragmentDirections.actionSongListsFragmentToMusicFragment(
//                            position,
//                            musicServices!!.serviceList[position].songId
//                        )
//                    navController.navigate(actions)
                } else {
                    musicServices?.get().let { it?.songSelectionList(3) }
//                    var actions: NavDirections =
//                        PlaylistsFragmentDirections.actionPlaylistsFragmentToMusicFragment(
//                            position,
//                            musicServices!!.serviceList[position].songId
//                        )
//                    navController.navigate(actions)
                }
            }
        })
    }
//    fun showPopup(mview: View, position: Int){
//        val popUp= PopupMenu(context, mview)
//        popUp.inflate(R.menu.options_menu)
//
//        if (value!=3) {
//            var item=popUp.menu.findItem(R.id.removeFromPlaylist)
//            item.isVisible = false
//        }
//
//        popUp.setOnMenuItemClickListener {
//
//            when (it.itemId){
//                R.id.addToPlayList -> {
//                    alertBoxForPlaylist(position)
//                    true
//                }
//                R.id.removeFromPlaylist ->{
//                    alertBoxForRemoval(position)
//                    true
//                }
//                else ->   {
//                    true
//                }
//
//            }
//        }
//        popUp.show()
//
//    }
//
//    private fun alertBoxForRemoval(position: Int) {
//
//
//        var alertDialog=AlertDialog.Builder(context)
//        alertDialog.setTitle("Delete")
//        alertDialog.setMessage("Are you sure to remove the song from playlist")
//
//
//
//
//
//        alertDialog.setPositiveButton("Yes",DialogInterface.OnClickListener { dialog, which ->
//
//            var name=musiclist[position].playlistName
//
//            Log.i("NamePlaylist1",name)
//
//            GlobalScope.launch (Dispatchers.IO){
//
//                musicFavDao.deleteSong(musiclist[position])
//                checkPlaylistNames=musicFavDao.readAllFiles() as MutableList
//
//
//                for (item in checkPlaylistNames){
//                    Log.i("NamePlaylist22",item)
//                }
//                Log.i("NamePlaylist","-----------")
//
//                withContext(Dispatchers.Main){
//                    musiclist.removeAt(position)
//                    this@MusicRecyclerAdapter.notifyDataSetChanged()
//                    Toast.makeText(context,"Song removed from playlist",Toast.LENGTH_SHORT).show()
//                 if (!checkPlaylistNames.contains(name)){
//                     Log.i("NamePlaylist","Contains")
//                     playlistFiles.remove(name)
//
//                     var actions: NavDirections =
//                             PlaylistsFragmentDirections.actionPlaylistsFragmentToMainFragment()
//                     navController.navigate(actions)
//                 }
//
//                }
//            }
//        })
//        alertDialog.show()
//
//
//    }

    fun readPlaylistFiles(){
        GlobalScope.launch(Dispatchers.IO) {
            playlistFiles=musicFavDao.readAllFiles() as MutableList

            withContext(Dispatchers.Main){
                if(playlistFiles.contains("favourites") || playlistFiles.contains("all")) {
                    playlistFiles.remove("favourites")
                    playlistFiles.remove("all")
                }
            }

        }
    }
//     fun readForCheckFiles(){
//        GlobalScope.launch(Dispatchers.IO) {
//
//
//            withContext(Dispatchers.Main){
//                for (item in checkPlaylistNames){
//                    Log.i("testnames",item)
//                }
//            }
//
//        }
//
//    }
////
//    private fun alertBoxForPlaylist(position: Int) {
//
//        val alertBox=LayoutInflater.from(context).inflate(R.layout.alert_box_layout, null)
//        val alertBoxBuilder= AlertDialog.Builder(context).setView(alertBox)
//        alertBoxBuilder.setCancelable(false)
//
//    Log.i("PlaylistFileName","Checking")
//
//        adapterF= PlaylistsRecyclerAdapter(playlistFiles,null)
//
//        val alertBoxInstance=alertBoxBuilder.show()
//        alertBox.alertBoxOk.setOnClickListener {
//            val playlistName=alertBox.playListNameEditText.text.toString()
//
//
//            if (!playlistFiles.contains(playlistName)){
//                playlistFiles.add(playlistName)
//                adapterF= PlaylistsRecyclerAdapter(playlistFiles,null)
//                adapterF.notifyDataSetChanged()
//                for (item in playlistFiles){
//                    Log.i("PlaylistFileName",item)
//                }
//
//            }
//            createPlaylistName(position, playlistName)
//            alertBoxInstance.dismiss()
//        }
//        if (playlistFiles.isNotEmpty()){
//            alertBox.playlistNamesRecommandations.apply {
//                adapter=adapterP
//                layoutManager=GridLayoutManager(view.context, 2)
//
//            }
//        }else alertBox.playlistNamesRecommandations.visibility=View.GONE
//        alertBox.alertBoxCancel.setOnClickListener {
//            alertBoxInstance.dismiss()
//        }
//    }
//    private fun createPlaylistName(position: Int, playlistName: String) {
////        adapterP!!.notifyDataSetChanged()
//        musiclist[position].playlistName=playlistName
//        musiclist[position].timeStamp=System.currentTimeMillis().toString()+musiclist[position].songId.toString()
//
//            try {
//                GlobalScope.launch(Dispatchers.IO) {
//                    if (!musicFavDao.checkForRepeatedSong(musiclist[position].songId, playlistName)){
//                        musicFavDao.addMusic(musiclist[position])
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show()
//                        }
//                    }else{
//                        withContext(Dispatchers.Main){
//                            Toast.makeText(
//                                context,
//                                "Selected song is already in the playlist",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//
//                }
//            }catch (e: Exception){
//                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
//            }
//
//
//
//
//
//    }

    override fun getItemCount(): Int {
        return musiclist.size
    }

}