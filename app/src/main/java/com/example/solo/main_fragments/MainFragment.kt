package com.example.solo.main_fragments

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.solo.R
import com.example.solo.adapters.HomeAdapter
import com.example.solo.adapters.PlaylistsRecyclerAdapter

import com.example.solo.database.musicDao
import com.example.solo.database.musicFavourites
import com.example.solo.databinding.FragmentMainBinding
import com.example.solo.modelclass.*
import com.example.solo.services.MusicServices
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Original withReload Problem
class MainFragment : Fragment(), ServiceConnection {



    lateinit var mainView: View
    lateinit var navController: NavController

    lateinit var musicDao: musicDao


    lateinit var mainFragmentBinding: FragmentMainBinding
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {



                val intent = Intent(activity, MusicServices::class.java)
                activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
                activity?.startService(intent)


        mainFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        mainView = mainFragmentBinding.root

        navController = findNavController()
        val adapter = activity?.let { HomeAdapter(it.supportFragmentManager, lifecycle, mainView) }

//        adapterP= PlaylistsRecyclerAdapter(playlistFiles,view)
        Log.i("Lifecycle", "MainFragment")
        mainFragmentBinding.viewPager2.adapter = adapter


        mainFragmentBinding.viewPager2.offscreenPageLimit=1
//        mainFragmentBinding.viewPager2.isSaveEnabled = false
        musicDao = musicFavourites.getDatabase(requireActivity().application).musicDao()
        TabLayoutMediator(mainFragmentBinding.tabLayout, mainFragmentBinding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Offline"
                1 -> tab.text = "Playlist"

            }

        }.attach()

        if (musicServices != null) {
            if (musicServices!!.mediaPlayer != null) {
                bottomSheetMethod()
            }
        }
        if (mainFragmentBinding.bottomSheetPlayer.visibility == View.VISIBLE) {
            mainFragmentBinding.bottomSheetPlayer.setOnClickListener {
                openMusicFragment()
            }
        }



        return mainFragmentBinding.root
    }


    private fun openMusicFragment() {
        var actions: NavDirections = MainFragmentDirections.actionMainFragmentToMusicFragment(musicServices!!.position, musicServices!!.lastPlayedSongId)
        navController.navigate(actions)
    }

    private fun setSongDetailsAtBottomSheet() {
        val uri= musicServices!!.serviceList[musicServices!!.position].imagePath
        val imageUri=Uri.parse(uri)
        mainFragmentBinding.bottomSheetSongName.text = musicServices!!.serviceList[musicServices!!.position].songName
        mainFragmentBinding.bottomSheetduration.text = formatDuration(musicServices!!.serviceList[musicServices!!.position].duration)
        Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).fitCenter().into(mainFragmentBinding.bottomSheetMusicThumbnail)
    }

    private fun bottomSheetMethod() {
        if (musicServices != null) {
            mainFragmentBinding.bottomSheetPlayer.visibility = View.VISIBLE
            Log.i("1234", "musicservice not null")
            mainFragmentBinding.bottomSheetSongName.isSelected = true
            if (musicServices!!.mediaPlayer != null) {

                val uri = musicServices!!.serviceList[musicServices!!.position].imagePath
                val imageUri = Uri.parse(uri)
                setSongDetailsAtBottomSheet()
                Log.i("1234", imageUri.toString())
                if (musicServices!!.mediaPlayer!!.isPlaying) mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)
                activity?.let { Glide.with(it).load(imageUri).placeholder(R.drawable.music_thumbnail).into(mainFragmentBinding.bottomSheetMusicThumbnail) }
                Log.i("1234",imageUri.toString())
                mainFragmentBinding.bottomSheetPlayButton.setOnClickListener {
                    if (musicServices!!.mediaPlayer!!.isPlaying) {
                        musicServices!!.pauseMusicSystem()
                        mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.play_button)
                    } else {
                        musicServices!!.playMusicSystem()
                        mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)
                    }
                }

                mainFragmentBinding.bottomSheetPreviousButton.setOnClickListener {

                    musicServices!!.methodForPreviousAndNext(false)
                    setSongDetailsAtBottomSheet()


                }

                mainFragmentBinding.bottomSheetNextButton.setOnClickListener {
                    musicServices!!.methodForPreviousAndNext(true)
                    setSongDetailsAtBottomSheet()

                }
            }
        }
    }



    override fun onResume() {
        super.onResume()


//        val adapter = activity?.let { HomeAdapter(it.supportFragmentManager, lifecycle, mainView) }


        for (item in playlistFiles){
            Log.i("sizee", playlistFiles.size.toString())
        }
//        val adapter = activity?.let { HomeAdapter(it.supportFragmentManager, lifecycle, mainView) }
//        mainFragmentBinding.viewPager2.adapter = adapter
        if (musicServices != null) {
            musicServices!!.isActive=true
            Log.i("Lifecycle", "MainOnResume")
            if (musicServices!!.mediaPlayer != null) {
                Log.i("MusicPosition", musicServices!!.position.toString())
                Log.i("MusicPosition", musicServices!!.serviceList.size.toString())
                bottomSheetMethod()
            }

//            musicServices!!.getAllSongs(requireActivity())
        }


    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        val binder = service as MusicServices.MyBinder
        musicServices = binder.currentBinder()

        musicServices!!.setFragment(this)
        musicServices!!.getAllSongs()
        Log.i("Lifecycle", "MainService")


    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }
}