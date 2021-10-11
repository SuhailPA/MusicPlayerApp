package com.example.solo.main_fragments


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
import com.example.solo.R
import com.example.solo.broadcast_class.NotificationReciever
import com.example.solo.database.musicDao
import com.example.solo.database.musicFavourites
import com.example.solo.databinding.FragmentMusicBinding
import com.example.solo.modelclass.formatDuration
import com.example.solo.modelclass.musicServices
import com.example.solo.services.MusicServices
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*


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





        if (musicServices != null && musicServices!!.lastPlayedSongId == args.songId) {

            setFavouriteIcon()
//            shuffleChecked()
            musicServices!!.position = args.position
            setLayout(args.position)
            musicBinding.seekBar2.progress = 0
            musicBinding.seekBar2.max = (musicServices!!.mediaPlayer!!.duration) / 1000
            musicServices!!.seekBarSetup(this)

            if (musicServices!!.mediaPlayer!!.isPlaying) musicBinding.playButton.setImageResource(R.drawable.pause_button)
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
                if (musicServices!!.mediaPlayer != null && fromUser) {
                    progressSeek = progress
                    musicServices!!.mediaPlayer!!.seekTo(progress * 1000)
                    musicServices!!.showNotification(R.drawable.pause_button)

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        musicBinding.shuffleIcon.setOnClickListener {
            Toast.makeText(context, "Shuffle Activated", Toast.LENGTH_SHORT).show()
            musicServices!!.serviceList.removeAt(args.position)
            musicServices!!.serviceList.shuffle()
        }





        musicBinding.timerSet.setOnClickListener {
            if(musicBinding.timerSelectionCard.visibility==View.VISIBLE){
                musicBinding.timerSelectionCard.visibility=View.GONE
            }else{
                musicBinding.timerSelectionCard.visibility=View.VISIBLE

                if(!musicServices!!.isTimer)musicBinding.timerOff.setImageResource(R.drawable.timer_off_disabled)

                musicBinding.timer10.setOnClickListener {
                    musicServices!!.isTimer=true
                    Toast.makeText(context, "MusicPlayer will be closed after 10 minutes", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.timerValueInSec=5000
                    musicServices!!.timerServices(5000, requireActivity())
                }

                musicBinding.timer15.setOnClickListener {
                    musicServices!!.isTimer=true
                    Toast.makeText(context, "MusicPlayer will be closed after 15 seconds", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.timerValueInSec=60000 * 15
                    musicServices!!.timerServices(60000 * 15, requireActivity())
                }

                musicBinding.timer30.setOnClickListener {
                    musicServices!!.isTimer=true
                    Toast.makeText(context, "MusicPlayer will be closed after 30 seconds", Toast.LENGTH_SHORT).show()
                    musicBinding.timerSet.setImageResource(R.drawable.timer_on)
                    musicBinding.timerSelectionCard.visibility=View.GONE
                    musicServices!!.timerValueInSec=60000 * 30
                    musicServices!!.timerServices(60000 * 30, requireActivity())
                }


                musicBinding.timerOff.setOnClickListener {
                    if (musicServices!!.isTimer){
                        Toast.makeText(context, "Timer has been switched off", Toast.LENGTH_SHORT).show()
                        musicBinding.timerSet.setImageResource(R.drawable.timer_off)
                        musicServices!!.isTimer=false
                    }

                    musicBinding.timerSelectionCard.visibility=View.GONE

                }
            }


        }


        musicBinding.playButton.setOnClickListener {
            if (musicServices!!.mediaPlayer!!.isPlaying) musicServices!!.pauseMusicSystem()
            else musicServices!!.playMusicSystem()
        }

        musicBinding.nextButton.setOnClickListener {
            musicServices!!.methodForPreviousAndNext(true)
            setFavouriteIcon()
//            shuffleChecked()
        }

        musicBinding.previousButton.setOnClickListener {
            musicServices!!.methodForPreviousAndNext(false)
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
        if(menu.hasVisibleItems() && menu.size()!=0){
            menu.findItem(R.id.ascending).isVisible = false
            menu.findItem(R.id.descending).isVisible=false
        }
    }

    private fun favouriteIconCliked() {

        if (!checkTheSongIsInFavourites()) {
            GlobalScope.launch(Dispatchers.IO) {
                musicServices!!.serviceList[musicServices!!.position].playlistName = "favourites"
                musicServices!!.serviceList[musicServices!!.position].timeStamp=System.currentTimeMillis().toString()+ musicServices!!.serviceList[musicServices!!.position].songId.toString()
                musicFavDao.addMusic(musicServices!!.serviceList[musicServices!!.position])

                Log.i("Favourites", "Song added")
                withContext(Dispatchers.Main) {
                    musicBinding.favouriteIcon.setImageResource(R.drawable.selected_as_favourite)
                    musicServices!!.getAllSongs()
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                musicFavDao.deleteSong(musicServices!!.serviceList[musicServices!!.position])
                withContext(Dispatchers.Main) {
                    musicBinding.favouriteIcon.setImageResource(R.drawable.favourites)
                    musicServices!!.getAllSongs()
                }
            }
        }
    }

    private fun setFavouriteIcon() {
        if (checkTheSongIsInFavourites()) musicBinding.favouriteIcon.setImageResource(R.drawable.selected_as_favourite)
        else musicBinding.favouriteIcon.setImageResource(R.drawable.favourites)

        if (musicServices!!.isTimer) musicBinding.timerSet.setImageResource(R.drawable.timer_on)
        else  musicBinding.timerSet.setImageResource(R.drawable.timer_off)

    }


    private fun checkTheSongIsInFavourites(): Boolean {
        for (song in musicServices!!.favouriteList) {
            if (musicServices!!.lastPlayedSongId == song.songId) {
                return true
            }
        }
        return false
    }

    private fun setLayout(position: Int) {

        var uri = musicServices!!.serviceList[position].imagePath
        var imageUri = Uri.parse(uri)
        Log.i("1234Fragment", imageUri.toString())
        if (imageUri != null) {
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail_blurred).fitCenter().apply(
                    RequestOptions.bitmapTransform(
                            BlurTransformation(25))
            ).into(musicBinding.backgroundImage)
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).into(musicBinding.mainMusicThumbnail)
        }
        musicBinding.totalDuration.text = formatDuration(musicServices!!.serviceList[position].duration)
    }

    fun createMediaPlayer() {
        try {
            Log.i("Service", "mediaplayer")

            if (musicServices!!.mediaPlayer == null)
                musicServices!!.mediaPlayer = MediaPlayer()
            musicServices!!.mediaPlayer!!.reset()



                musicServices!!.mediaPlayer!!.setDataSource(musicServices!!.serviceList[musicServices!!.position].songPath)

            musicServices!!.mediaPlayer!!.prepare()
            musicServices!!.mediaPlayer!!.start()
            musicBinding.playButton.setImageResource(R.drawable.pause_button)
            musicBinding.totalDuration.text = formatDuration(musicServices!!.mediaPlayer!!.duration.toLong())
            musicBinding.timeCount.text = formatDuration(musicServices!!.mediaPlayer!!.currentPosition.toLong())
            musicBinding.seekBar2.progress = 0
            musicBinding.seekBar2.max = (musicServices!!.mediaPlayer!!.duration) / 1000
            musicServices!!.mediaPlayer!!.setOnCompletionListener {
                musicServices!!.methodForPreviousAndNext(true)
                setFavouriteIcon()
            }
            musicServices!!.showNotification(R.drawable.pause_button)

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
        musicServices = binder.currentBinder()
        Log.i("testPosition", args.position.toString())

        musicServices!!.lastPlayedSongId = args.songId
//        musicServices!!.serviceList= musicServices!!.allMusicList as MutableList<MusicModelClass>
        setFavouriteIcon()
        Log.i("PositionCheck", musicServices!!.serviceList[args.position].songName)

//        musicServices!!.isShuffled=false
        musicServices!!.seekBarSetup(this)
        musicServices!!.position = args.position
        musicServices!!.setLayout(musicServices!!.position)
        createMediaPlayer()
    }




    override fun onServiceDisconnected(name: ComponentName?) {
        musicServices = null
        Log.i("Service", "Start disconnected")
    }
}