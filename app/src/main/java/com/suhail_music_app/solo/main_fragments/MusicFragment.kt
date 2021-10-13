package com.suhail_music_app.solo.main_fragments


import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.broadcast_class.NotificationReciever
import com.suhail_music_app.solo.database.musicDao
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.databinding.FragmentMusicBinding
import com.suhail_music_app.solo.modelclass.formatDuration
import com.suhail_music_app.solo.modelclass.musicServices

import com.suhail_music_app.solo.services.MusicServices
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.favourite_fragment.*
import kotlinx.coroutines.*
import java.lang.ref.WeakReference


class MusicFragment : Fragment(), ServiceConnection {

    lateinit var musicBinding: FragmentMusicBinding


    lateinit var musicFavDao: musicDao
    private val args by navArgs<MusicFragmentArgs>()

    //    lateinit var list: List<MusicModelClass>
    lateinit var uri: Uri

    //    var position: Int = -1
    var progressSeek: Int = -1
    private lateinit var runnable: Runnable


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        Log.i("NumberOfExecution", "1")
        musicFavDao = musicFavourites.getDatabase(requireActivity().application).musicDao()

        // Inflate the layout for this fragment
        musicBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music, container, false)

        if (musicServices != null && musicServices!!.get().let { it?.lastPlayedSongId== args.songId }) {

            setFavouriteIcon()
//            shuffleChecked()
            musicServices!!.get().let { it?.position=args.position }
            setLayout(args.position)
            musicBinding.seekBar2.progress = 0
            musicBinding.seekBar2.max = (musicServices!!.get().let { it?.mediaPlayer!!.duration })/1000
            val weakMusicFragment=WeakReference(this)
            musicServices!!.get().let { it?.seekBarSetup(weakMusicFragment) }

            if (musicServices!!.get().let { it?.mediaPlayer!!.isPlaying }) musicBinding.playButton.setImageResource(R.drawable.pause_button)
            else musicBinding.playButton.setImageResource(R.drawable.play_button)
        } else {
            Log.i("MusicFragment", "Executes2")
            val intent = Intent(activity, MusicServices::class.java)
            activity?.bindService(intent, this, BIND_AUTO_CREATE)
            activity?.startService(intent)
        }

        musicBinding.favouriteIcon.setOnClickListener {
            favouriteIconCliked()
        }
        setHasOptionsMenu(true)
        musicBinding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (musicServices!!.get().let { it?.mediaPlayer!=null  }&& fromUser) {
                    progressSeek = progress
                    musicServices!!.get().let { it?.mediaPlayer?.seekTo(progress*1000) }

                    if (musicServices!!.get().let { it?.mediaPlayer!!.isPlaying })musicServices!!.get().let { it?.showNotification(R.drawable.pause_button) }
                    else musicServices!!.get().let { it?.showNotification(R.drawable.play_button) }


                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        musicBinding.shuffleIcon.setOnClickListener {
            Toast.makeText(context, "Shuffle Activated", Toast.LENGTH_SHORT).show()
            musicServices!!.get().let { it?.serviceList?.removeAt(args.position) }
            musicServices!!.get().let { it?.serviceList?.shuffle() }
        }





        musicBinding.timerSet.setOnClickListener {
            if(musicBinding.timerSelectionCard.visibility==View.VISIBLE){
                musicBinding.timerSelectionCard.visibility=View.GONE
            }else{
                musicBinding.timerSelectionCard.visibility=View.VISIBLE

                if(musicServices!!.get().let { it?.isTimer==false })musicBinding.timerOff.setImageResource(R.drawable.timer_off_disabled)

                musicBinding.timer10.setOnClickListener {
                    musicServices!!.get().let { it?.isTimer=true }
                    Toast.makeText(context, "MusicPlayer will be closed after 10 minutes", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.get().let { it?.timerValueInSec=60000 * 10 }
                    musicServices!!.get().let { it?.timerServices(60000 *10) }
                }

                musicBinding.timer15.setOnClickListener {
                    musicServices!!.get().let { it?.isTimer=true }
                    Toast.makeText(context, "MusicPlayer will be closed after 15 seconds", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.get().let { it?.timerValueInSec=60000 * 15 }
                    musicServices!!.get().let { it?.timerServices(60000 *15) }
                }

                musicBinding.timer30.setOnClickListener {
                    musicServices!!.get().let { it?.isTimer=true }
                    Toast.makeText(context, "MusicPlayer will be closed after 30 seconds", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.get().let { it?.timerValueInSec=60000 * 30 }
                    musicServices!!.get().let { it?.timerServices(60000 *30) }
                }


                musicBinding.timerOff.setOnClickListener {
                    if (musicServices!!.get().let { it!!.isTimer }){
                        Toast.makeText(context, "Timer has been switched off", Toast.LENGTH_SHORT).show()
                        musicBinding.timerSet.setImageResource(R.drawable.timer_off)
                        musicServices!!.get().let { it?.isTimer=false }
                    }

                    musicBinding.timerSelectionCard.visibility=View.GONE

                }
            }


        }


        musicBinding.playButton.setOnClickListener {
            if (musicServices!!.get().let { it?.mediaPlayer!!.isPlaying }) musicServices!!.get().let { it?.pauseMusicSystem() }
            else musicServices!!.get().let { it?.playMusicSystem() }
        }

        musicBinding.nextButton.setOnClickListener {
            musicServices!!.get().let { it?.methodForPreviousAndNext(true) }
            setFavouriteIcon()
//            shuffleChecked()
        }

        musicBinding.previousButton.setOnClickListener {
            musicServices!!.get().let { it?.methodForPreviousAndNext(false) }
            setFavouriteIcon()
//            shuffleChecked()
        }


        return musicBinding.root
    }
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val myNoisyAudioStreamReceiver = NotificationReciever()

    private val callback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            requireActivity().registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
        }

        override fun onStop() {
            requireActivity().unregisterReceiver(myNoisyAudioStreamReceiver)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun favouriteIconCliked() {

        if (!checkTheSongIsInFavourites()) {
            GlobalScope.launch(Dispatchers.IO) {
                musicServices!!.get().let { it!!.serviceList[it.position].playlistName ="favourites"}
                musicServices!!.get().let { it!!.serviceList[it.position].timeStamp=System.currentTimeMillis().toString()+ it.serviceList[it.position].songId.toString() }
                musicFavDao.addMusic(musicServices!!.get().let { it!!.serviceList[it.position] })

                Log.i("Favourites", "Song added")
                withContext(Dispatchers.Main) {
                    musicBinding.favouriteIcon.setImageResource(R.drawable.selected_as_favourite)
                    musicServices!!.get().let { it?.getAllSongs() }
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                musicFavDao.deleteSong(musicServices!!.get().let { it!!.serviceList[it.position] })
                withContext(Dispatchers.Main) {
                    musicBinding.favouriteIcon.setImageResource(R.drawable.favourites)
                    musicServices!!.get().let { it?.getAllSongs() }
                }
            }
        }
    }

    private fun setFavouriteIcon() {
        if (checkTheSongIsInFavourites()) musicBinding.favouriteIcon.setImageResource(R.drawable.selected_as_favourite)
        else musicBinding.favouriteIcon.setImageResource(R.drawable.favourites)

        if (musicServices!!.get().let { it!!.isTimer }) musicBinding.timerSet.setImageResource(R.drawable.timer_on)
        else  musicBinding.timerSet.setImageResource(R.drawable.timer_off)

    }


    private fun checkTheSongIsInFavourites(): Boolean {
        for (song in musicServices!!.get().let { it!!.favouriteList }){
            if (musicServices!!.get().let { it?.lastPlayedSongId==song.songId }) {
                return true
            }
        }
        return false
    }

    private fun setLayout(position: Int) {

        var uri = musicServices!!.get().let { it!!.serviceList[position].imagePath }
        var imageUri = Uri.parse(uri)
        Log.i("1234Fragment", imageUri.toString())
        if (imageUri != null) {
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail_blurred).fitCenter().apply(
                    RequestOptions.bitmapTransform(
                            BlurTransformation(25))
            ).into(musicBinding.backgroundImage)
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).into(musicBinding.mainMusicThumbnail)
        }
        musicBinding.totalDuration.text = formatDuration(musicServices!!.get().let { it!!.serviceList[position].duration })
    }

    fun createMediaPlayer() {
        try {
            Log.i("Service", "mediaplayer")

            if (musicServices!!.get().let { it?.mediaPlayer==null }){

                musicServices!!.get().let { it?.mediaPlayer=MediaPlayer() }
            }

            musicServices!!.get().let { it?.mediaPlayer!!.reset() }



                musicServices!!.get().let { it?.mediaPlayer?.setDataSource(it.serviceList[it.position].songPath) }

            musicServices!!.get().let { it?.mediaPlayer!!.prepare() }
            musicServices!!.get().let { it?.mediaPlayer?.start() }
            musicBinding.playButton.setImageResource(R.drawable.pause_button)
            musicBinding.totalDuration.text =
                musicServices!!.get().let { it?.mediaPlayer?.duration?.toLong() }.let {
                    formatDuration(
                        it!!
                    )
                }
            musicBinding.timeCount.text =
                musicServices!!.get().let { it?.mediaPlayer?.currentPosition?.toLong() }.let {
                    formatDuration(
                        it!!
                    )
                }
            musicBinding.seekBar2.progress = 0
            musicBinding.seekBar2.max = (musicServices!!.get().let { it?.mediaPlayer?.duration })!! / 1000
            musicServices?.get().let { it?.mediaPlayer?.setOnCompletionListener {
                musicServices?.get().let { it?.methodForPreviousAndNext(true)
                setFavouriteIcon()}
            } }
            musicServices?.get().let { it?.showNotification(R.drawable.pause_button) }

        } catch (e: Exception) {
            Log.i("CreatePlayerException", e.toString())
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home ->
                findNavController().popBackStack()
        }

        return true
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicServices = WeakReference(binder.currentBinder())
        Log.i("testPosition", args.position.toString())


        musicServices?.get().let { it?.lastPlayedSongId=args.songId }
//        musicServices!!.serviceList= musicServices!!.allMusicList as MutableList<MusicModelClass>
        setFavouriteIcon()
        Log.i("PositionCheck", musicServices?.get().let { it!!.serviceList[args.position].songName })


//        musicServices!!.isShuffled=false
        val weakMusicFragment=WeakReference(this)
        musicServices?.get().let { it?.musicActivity=weakMusicFragment }
        musicServices?.get().let { it?.seekBarSetup(weakMusicFragment) }
        musicServices?.get().let { it?.position=args.position }
        musicServices?.get().let { it?.setLayout(it.position) }
        createMediaPlayer()
    }




    override fun onServiceDisconnected(name: ComponentName?) {
        musicServices = null
        Log.i("Service", "Start disconnected")
    }
}