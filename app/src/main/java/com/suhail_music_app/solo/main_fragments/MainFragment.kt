package com.suhail_music_app.solo.main_fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.adapters.HomeAdapter
import com.suhail_music_app.solo.database.musicDao
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.databinding.FragmentMainBinding
import com.suhail_music_app.solo.modelclass.*
import com.suhail_music_app.solo.services.MusicServices
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.ref.WeakReference

//Original withReload Problem
class MainFragment : Fragment(), ServiceConnection {



      lateinit var mainView: WeakReference<View>
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


        mainFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        mainView = WeakReference(mainFragmentBinding.root)



        navController = findNavController()
        val adapter = activity?.let { mainView.get()
            ?.let { it1 -> HomeAdapter(it.supportFragmentManager, lifecycle, it1) } }

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
            if (musicServices?.get().let { it?.mediaPlayer!=null }) {
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
        var actions: NavDirections = MainFragmentDirections.actionMainFragmentToMusicFragment(
            musicServices?.get().let { it?.position!! },
            musicServices?.get().let { it?.lastPlayedSongId!! }
        )
        navController.navigate(actions)
    }

    private fun setSongDetailsAtBottomSheet() {
        val uri= musicServices?.get().let { it?.serviceList?.get(it.position)?.imagePath }
        val imageUri=Uri.parse(uri)

        mainFragmentBinding.bottomSheetSongName.text = musicServices?.get().let { it?.serviceList?.get(it.position)?.songName }
        mainFragmentBinding.bottomSheetduration.text = musicServices?.get().let { it?.serviceList?.get(it.position)?.duration }?.let { formatDuration(it) }
        Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).fitCenter().into(
            mainFragmentBinding.bottomSheetMusicThumbnail
        )
    }

    private fun bottomSheetMethod() {
        if (musicServices != null) {
            mainFragmentBinding.bottomSheetPlayer.visibility = View.VISIBLE
            Log.i("1234", "musicservice not null")
            mainFragmentBinding.bottomSheetSongName.isSelected = true
            if (musicServices?.get().let { it?.mediaPlayer!=null }) {

                val uri = musicServices?.get().let { it?.serviceList?.get(it.position)?.imagePath }
                val imageUri = Uri.parse(uri)
                setSongDetailsAtBottomSheet()
                Log.i("1234", imageUri.toString())
                if (musicServices?.get().let { it?.mediaPlayer?.isPlaying }!!) mainFragmentBinding.bottomSheetPlayButton.setImageResource(
                    R.drawable.pause_button
                )
                activity?.let { Glide.with(it).load(imageUri).placeholder(R.drawable.music_thumbnail).into(
                    mainFragmentBinding.bottomSheetMusicThumbnail
                ) }
                Log.i("1234", imageUri.toString())
                mainFragmentBinding.bottomSheetPlayButton.setOnClickListener {
                    if (musicServices?.get().let { it?.mediaPlayer?.isPlaying }!!) {
                        musicServices?.get().let { it?.pauseMusicSystem() }
                        mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.play_button)
                    } else {
                        musicServices?.get().let { it?.playMusicSystem() }
                        mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)
                    }
                }

                mainFragmentBinding.bottomSheetPreviousButton.setOnClickListener {


                    musicServices?.get().let { it?.methodForPreviousAndNext(false) }
                    setSongDetailsAtBottomSheet()

                    if (musicServices?.get().let { it?.mediaPlayer?.isPlaying!! })mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)
                    else mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.play_button)


                }

                mainFragmentBinding.bottomSheetNextButton.setOnClickListener {

                    musicServices?.get().let { it?.methodForPreviousAndNext(true) }
                    setSongDetailsAtBottomSheet()
                    if (musicServices?.get().let { it?.mediaPlayer?.isPlaying!! })mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)
                    else mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.play_button)
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
            musicServices?.get().let { it?.isActive=true }
            Log.i("Lifecycle", "MainOnResume")
            if (musicServices?.get().let { it?.mediaPlayer!=null }) {
                bottomSheetMethod()
            }
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        val binder = service as MusicServices.MyBinder
        musicServices = WeakReference(binder.currentBinder())


        val weakMainFragment=WeakReference(this)
        musicServices?.get().let { it?.mainFragment=weakMainFragment}
        musicServices?.get().let { it?.getAllSongs() }
        Log.i("Lifecycle", "MainService")


    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
//        mainView.clear()
//        musicServices?.get().let { it?.mainFragment?.clear()}
        mainFragmentBinding.unbind()
    }
}