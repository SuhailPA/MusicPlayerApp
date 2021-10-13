package com.suhail_music_app.solo.main_fragments

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.suhail_music_app.solo.MainActivity
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.adapters.MusicRecyclerAdapter
import com.suhail_music_app.solo.database.musicDao
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.databinding.FragmentSongListsBinding
import com.suhail_music_app.solo.modelclass.MusicModelClass
import com.suhail_music_app.solo.modelclass.musicServices
import com.suhail_music_app.solo.modelclass.playlistFiles
import com.suhail_music_app.solo.services.MusicServices
import kotlinx.android.synthetic.main.alert_box_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


class SongListsFragment : Fragment(), ServiceConnection {

    lateinit var songListBinding: FragmentSongListsBinding
    lateinit var songLists: List<MusicModelClass>
    lateinit var musicDao: musicDao
    lateinit var mainActivity: MainActivity
    var checkPlaylistNames= mutableListOf<String>()

    lateinit var navController: NavController
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.i("ResumeTest", "OnCreateView")

        mainActivity= MainActivity()
        navController=findNavController()
        // Inflate the layout for this fragment
        songListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_song_lists, container, false)
        musicDao = musicFavourites.getDatabase(requireActivity().application).musicDao()
        setHasOptionsMenu(true)

        return songListBinding.root
    }

    private val onClicked:(MusicModelClass,Int)->Unit={ musicModelClass: MusicModelClass, i: Int ->
        var actions: NavDirections =
                SongListsFragmentDirections.actionSongListsFragmentToMusicFragment(
                        i,
                        musicModelClass.songId
                )
        navController.navigate(actions)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("ResumeTest", "OnCreate")
        val intent = Intent(activity, MusicServices::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)
    }

    private fun getAllSongs() {
        songLists= emptyList()
        songLists = musicServices!!.get().let { it?.favouriteList!! }
        if (songLists.isNotEmpty()){
            var adapterF = MusicRecyclerAdapter(songLists as MutableList<MusicModelClass>, requireActivity(), songListBinding.root, 2,onClicked,popupOnClick)
            songListBinding.playlistSongLists.apply {
                songListBinding.ifThereIsNoSongs.visibility=View.GONE
                visibility=View.VISIBLE
                adapter = adapterF
                layoutManager = LinearLayoutManager(requireContext())
                Log.i("visiblity", "yes")
            }
        }else {
            songListBinding.ifThereIsNoSongs.visibility=View.VISIBLE
            Log.i("visiblity", "no")
        }

    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.IO) {

            Log.i("CouroutineTest", "1")
            withContext(Dispatchers.Main) {
                Log.i("CouroutineTest", "3")
                getAllSongs()
            }
        }


    }


    private fun ascendingOrderedSort(){
        var sortedList= mutableListOf<MusicModelClass>()
        if ( sortedList.isNotEmpty())sortedList.clear()
        sortedList.addAll(songLists)
        sortedList.sortBy { it.songName }
        var adapterF = MusicRecyclerAdapter(sortedList, requireActivity(), songListBinding.root, 2,onClicked,popupOnClick)
        songListBinding.playlistSongLists.apply {
            songListBinding.ifThereIsNoSongs.visibility=View.GONE
            visibility=View.VISIBLE
            adapter = adapterF
            layoutManager = LinearLayoutManager(requireContext())
            Log.i("CouroutineTest", "4")
        }
    }

    private fun descendingOrderedSort(){
        var discendingOrderedList= mutableListOf<MusicModelClass>()

        if (discendingOrderedList.isNotEmpty()) discendingOrderedList.clear()
        discendingOrderedList.addAll(songLists)
        discendingOrderedList.sortByDescending { it.songName }
        var adapterF = MusicRecyclerAdapter(discendingOrderedList, requireActivity(), songListBinding.root, 2,onClicked,popupOnClick)
        songListBinding.playlistSongLists.apply {
            songListBinding.ifThereIsNoSongs.visibility=View.GONE
            visibility=View.VISIBLE
            adapter = adapterF
            layoutManager = LinearLayoutManager(requireContext())
            Log.i("CouroutineTest", "4")
        }
    }


    private var popupOnClick:(View,Int,MutableList<MusicModelClass>)->Unit={ view: View, i: Int, musicLists: MutableList<MusicModelClass> ->
        showPopup(view,i,musicLists)
    }

    fun showPopup(mview: View, position: Int,items:MutableList<MusicModelClass>){
        val popUp= PopupMenu(requireContext(), mview)
        popUp.inflate(R.menu.options_menu)

        var item1=popUp.menu.findItem(R.id.addToPlayList)
        item1.isVisible=false
        var item=popUp.menu.findItem(R.id.removeFromPlaylist)
        item.isVisible = false



        popUp.setOnMenuItemClickListener {

            when (it.itemId){

                R.id.removeFromPlaylist ->{
                    alertBoxForRemoval(position,items)
                    true
                }
                else ->   {
                    true
                }

            }
        }
        popUp.show()

    }
//
//    private fun alertBoxForPlaylist(position: Int,item: MusicModelClass) {
//
//        val alertBox = LayoutInflater.from(requireActivity()).inflate(R.layout.alert_box_layout, null)
//        val alertBoxBuilder = AlertDialog.Builder(requireActivity()).setView(alertBox)
//        alertBoxBuilder.setCancelable(false)
//
//        Log.i("PlaylistFileName", "Checking")
////        adapterF = PlaylistsRecyclerAdapter(playlistFiles, null)
//
//        val alertBoxInstance = alertBoxBuilder.show()
//        alertBox.alertBoxOk.setOnClickListener {
//            val playlistName = alertBox.playListNameEditText.text.toString()
//
//
//            if (!playlistFiles.contains(playlistName)) {
//                playlistFiles.add(playlistName)
////                adapterF = PlaylistsRecyclerAdapter(playlistFiles, null)
////                adapterF.notifyDataSetChanged()
//                for (item in playlistFiles) {
//                    Log.i("PlaylistFileName", item)
//                }
//
//            }
//            createPlaylistName(position, playlistName,item)
//            alertBoxInstance.dismiss()
//        }
//
//        alertBox.alertBoxCancel.setOnClickListener {
//            alertBoxInstance.dismiss()
//
//        }
//    }
//    private fun createPlaylistName(position: Int, playlistName: String,item: MusicModelClass) {
////        adapterP!!.notifyDataSetChanged()
//        item.playlistName=playlistName
//        item.timeStamp=System.currentTimeMillis().toString()+item.songId.toString()
//
//        try {
//            GlobalScope.launch(Dispatchers.IO) {
//                if (!musicDao.checkForRepeatedSong(item.songId, playlistName)){
//                    musicDao.addMusic(item)
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show()
//                    }
//                }else{
//                    withContext(Dispatchers.Main){
//                        Toast.makeText(
//                                context,
//                                "Selected song is already in the playlist",
//                                Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//            }
//        }catch (e: Exception){
//            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
//        }
//
//
//
//
//
//    }
//
    private fun alertBoxForRemoval(position: Int,items:MutableList<MusicModelClass>) {


        var alertDialog= AlertDialog.Builder(context)
        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Are you sure to remove the song from playlist")





        alertDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->

            var name=items[position].playlistName

            Log.i("NamePlaylist1",name)

            GlobalScope.launch (Dispatchers.IO){

                musicDao.deleteSong(items[position])
                checkPlaylistNames=musicDao.readAllFiles() as MutableList


                for (item in checkPlaylistNames){
                    Log.i("NamePlaylist22",item)
                }
                Log.i("NamePlaylist","-----------")

                withContext(Dispatchers.Main){
                    items.removeAt(position)
//                    adapterS!!.notifyDataSetChanged()
                    Toast.makeText(context,"Song removed from playlist", Toast.LENGTH_SHORT).show()
                    if (!checkPlaylistNames.contains(name)){
                        Log.i("NamePlaylist","Contains")
                        playlistFiles.remove(name)

                    }

                }
            }
        })
        alertDialog.show()


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.ascending->ascendingOrderedSort()
            R.id.descending->descendingOrderedSort()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder

        musicServices = WeakReference(binder.currentBinder())
        Log.i("ResumeTest", "2")
        getAllSongs()
//
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }
}