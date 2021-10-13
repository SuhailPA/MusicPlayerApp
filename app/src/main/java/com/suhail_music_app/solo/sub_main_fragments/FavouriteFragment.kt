package com.suhail_music_app.solo.sub_main_fragments

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.adapters.PlaylistsRecyclerAdapter
import com.suhail_music_app.solo.database.musicDao
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.databinding.FavouriteFragmentBinding
import com.suhail_music_app.solo.main_fragments.MainFragmentDirections
import com.suhail_music_app.solo.modelclass.musicServices


import com.suhail_music_app.solo.modelclass.playlistFiles
import com.suhail_music_app.solo.services.MusicServices
import kotlinx.coroutines.*
import java.lang.ref.WeakReference


class FavouriteFragment(mainView: View) : Fragment(), ServiceConnection {

    lateinit var favouriteBinding: FavouriteFragmentBinding

    lateinit var adapterF:PlaylistsRecyclerAdapter
    var navController: NavController=Navigation.findNavController(mainView)
    lateinit var musicDao: musicDao



    lateinit var stdDao:musicDao
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.i("onCreateViewWorking", "2")
        favouriteBinding = DataBindingUtil.inflate(inflater, R.layout.favourite_fragment, container, false)




        val intent = Intent(activity, MusicServices::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)
         stdDao = musicFavourites.getDatabase(requireActivity().application).musicDao()
        favouriteBinding.favouriteSection.setOnClickListener {
            var actions=MainFragmentDirections.actionMainFragmentToSongListsFragment()
            navController.navigate(actions)
        }
        setPlaylistFiles()



        return favouriteBinding.root
    }

    private val onLongClick:(Int,MutableList<String>)->Unit={ i: Int,musicModelClass:MutableList<String> ->
        alertBox(i,musicModelClass)
    }

    private fun alertBox(position: Int,item: MutableList<String>){
        var alertDialog= AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Are you sure to delete the playlist")
        alertDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deletePlaylist(position,item)
        })

        alertDialog.setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        alertDialog.show()
    }

    private fun deletePlaylist(position:Int,item:MutableList<String>){

        GlobalScope.launch(Dispatchers.Main){
            musicServices!!.get().let { it?.deleteWholePlaylist(playlistFiles[position]) }
            withContext(Dispatchers.Main){
                var listName:String?=item[position]
                item.removeAt(position)
                if (playlistFiles.contains(listName)) playlistFiles.remove(listName)
                adapterF.notifyDataSetChanged()

                Toast.makeText(requireActivity(),"Successfully deleted",Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setPlaylistFiles(){
        GlobalScope.launch(Dispatchers.IO) {

            Log.i("ReadingFiles", "12323")
            playlistFiles = stdDao.readAllFiles() as MutableList<String>
//            playlistFiles.sortDescending()
            for (item in playlistFiles)Log.i("orderCheck",item)
            withContext(Dispatchers.Main) {
                Log.i("checkingWorkingTime","sdf")
                adapterF= PlaylistsRecyclerAdapter(playlistFiles,onLongClick)
                if(playlistFiles.contains("favourites"))playlistFiles.remove("favourites")
                favouriteBinding.favouritesList.apply {
                    adapter = adapterF
                    layoutManager = GridLayoutManager(activity, 2)
                }
            }

        }
    }

    override fun onStop() {
        super.onStop()
        Log.i("adapter_setting","stop")
    }

    override fun onPause() {
        super.onPause()
        Log.i("adapter_setting","Pause")
    }


    override fun onResume() {
        super.onResume()
//        setPlaylistFiles()
        adapterF= PlaylistsRecyclerAdapter(playlistFiles,onLongClick)
        favouriteBinding.favouritesList.adapter=adapterF
    }





    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicServices = WeakReference(binder.currentBinder())
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }


}