package com.suhail_music_app.solo.services

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.suhail_music_app.solo.MainActivity
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.broadcast_class.NotificationReciever
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.main_fragments.MainFragment
import com.suhail_music_app.solo.main_fragments.MainFragmentDirections
import com.suhail_music_app.solo.main_fragments.MusicFragment
import com.suhail_music_app.solo.modelclass.MusicModelClass
import com.suhail_music_app.solo.modelclass.formatDuration
import com.suhail_music_app.solo.notification.ApplicationClass
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.util.*


class MusicServices : Service() {

    var lastPlayedSongId: Int = -1
    lateinit var mainActivity:WeakReference<MainActivity>
     var serviceList = mutableListOf<MusicModelClass>()
    lateinit var allMusicList :List<MusicModelClass>
     var timerValueInSec:Long=0L
    lateinit var favouriteList: List<MusicModelClass>
    var isActive=true
     var handler:Handler?=null
    lateinit var playlistSongs:List<MusicModelClass>
    var position = -1
    var isTimer=false
      var runnable: Runnable?=null
   var mediaPlayer:MediaPlayer?=null
    private var mybinder = MyBinder()
    lateinit var musicActivity: WeakReference<MusicFragment>
    lateinit var mainFragment: WeakReference<MainFragment>
    lateinit var mediaSession: MediaSessionCompat
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return mybinder
    }

    fun navigationFromMainFragmentToPlaylistFragment(playlistName: String){
        if (mainFragment.get().let { it?.mainView!=null }){
            val navController: NavController = mainFragment.get()?.mainView?.get()?.let {
                Navigation.findNavController(
                        it
                )
            }!!
            val actions= MainFragmentDirections.actionMainFragmentToPlaylistsFragment(playlistName)
            navController.navigate(actions)
        }
    }



    fun playMusicSystem() {
        Log.i("PlayMusic", "song")
        musicActivity.get()?.let { it.musicBinding.playButton.setImageResource(R.drawable.pause_button) }
//        isPlaying = true
        mediaPlayer?.start()
        showNotification(R.drawable.pause_button)
    }






    fun pauseMusicSystem() {
        musicActivity.get().let {
            it?.musicBinding?.playButton?.setImageResource(R.drawable.play_button)
        }
//        isPlaying = false
        mediaPlayer?.pause()

        showNotification(R.drawable.play_button)
    }

    fun methodForPreviousAndNext(value: Boolean) {

        setSongPosition(value)
        setLayout(position)
        mainFragment.get().let {
            if (it!=null)setSongDetailsAtBottomSheet()
        }



        musicActivity.get().let {
            it?.createMediaPlayer()
        }
        lastPlayedSongId= serviceList[position].songId
    }

    fun timerServices(timerValue: Long){
        Thread{Thread.sleep(timerValue)
            Log.i("ThreadTest", "working")
            if (isTimer && timerValueInSec==timerValue){
                isTimer=false
                if (mediaPlayer!=null && mainActivity.get().let { it!!.isDestroyed }){
                    mediaPlayer?.pause()

                    mediaPlayer?.stop()

                    stopForeground(true)
                    mediaPlayer?.release()

                }else {
                    if ( musicActivity.get().let { it!!.isVisible }) {
                        mainActivity.get().let {
                            it?.runOnUiThread{
                                updateDrawableImage()
                            }
                        }
                    }
                    mainActivity.get().let {
                        it?.runOnUiThread{
                            Toast.makeText(it,"Since the application is not destroyed the timer will be disabled",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }.start()

    }

    private fun updateDrawableImage() {
        musicActivity.get().let {
            it?.musicBinding?.timerSet?.setImageResource(R.drawable.timer_off)
        }
    }


    fun setLayout(position: Int) {
      var uri:String


            uri = serviceList[position].imagePath
            musicActivity.get().let {
                if (it != null) {
                    it.musicBinding.totalDuration.text= formatDuration(serviceList[position].duration)
                }
            }


        var imageUri = Uri.parse(uri)
        Log.i("1234Fragment", imageUri.toString())
        if (imageUri != null) {
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail_blurred).apply(
                    RequestOptions.bitmapTransform(
                            BlurTransformation(25))
            ).into(musicActivity.get().let { it!!.musicBinding.backgroundImage })
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).into(musicActivity.get().let { it!!.musicBinding.mainMusicThumbnail })
        }

    }


    fun seekBarSetup(activity: WeakReference<MusicFragment>) {
        this.musicActivity = activity

        runnable = Runnable {
                musicActivity.get().let { it?.musicBinding?.timeCount?.text= formatDuration(mediaPlayer?.currentPosition!!.toLong())}
                musicActivity.get().let { it?.musicBinding?.seekBar2?.progress =
                    mediaPlayer?.currentPosition?.div(1000)!!
                }
                handler=Handler(Looper.getMainLooper())
                    handler!!.postDelayed(runnable!!, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable!!, 0)
    }


    fun getAllSongs() {
        val stdDao = musicFavourites.getDatabase(application).musicDao()
        GlobalScope.launch(Dispatchers.IO) {
            favouriteList = emptyList()
            favouriteList = stdDao.readAllMusic()
        }
    }

    fun getPlaylistSongs(name: String) {
        val stdDao = musicFavourites.getDatabase(application).musicDao()
            playlistSongs = emptyList()
            playlistSongs = stdDao.readSongsFromPlaylist(name)
    }

   suspend fun deleteWholePlaylist(name: String){
        val stdDao = musicFavourites.getDatabase(application).musicDao()
        stdDao.deletePlaylist(name)
    }
    fun showNotification(playPauseButton: Int) {

        val args = Bundle()

        args.putInt("position", position)
        args.putInt("songId", lastPlayedSongId)
//        args.putParcelableArray("allMusicLists", serviceList.toTypedArray())

        val pendingIntent = NavDeepLinkBuilder(baseContext)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.home_nav_graph)
                .setDestination(R.id.musicFragment)
                .setArguments(args)
                .createPendingIntent()

        val prevIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val songThumb=getImgArt(serviceList[position].songPath)
        val image = if (songThumb!=null) {
            BitmapFactory.decodeByteArray(songThumb, 0, songThumb.size)
        }else{
                BitmapFactory.decodeResource(resources,R.drawable.music_thumbnail_blurred)
            }


        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL1)
                .setContentIntent(pendingIntent)
                .setContentTitle(serviceList[position].songName)
                .setContentText(serviceList[position].artistName)
                .setSmallIcon(R.drawable.playlist_background)
                .setLargeIcon(image)
                .setStyle(NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(2,3)
                        .setMediaSession(mediaSession.sessionToken))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_baseline_close_24, "Exit", exitPendingIntent)
                .addAction(R.drawable.previous_button, "Previous", prevPendingIntent)
                .addAction(playPauseButton, "Play", playPendingIntent)
                .addAction(R.drawable.next_black, "Next", nextPendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (mediaPlayer!!.isPlaying ) 1F else 0F
            if (playbackSpeed==1F)Log.i("sktest", "playing")
            else Log.i("sktest", "pause")
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                    .build())
            val playBackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            Log.i("notificationSeek", "outside")
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    Log.i("notificationSeek", "inside")
                    val playBackStateNew = mediaPlayer!!.currentPosition.toLong().let {
                        PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, it, playbackSpeed)
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .build()
                    }
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }
        startForeground(13, notification)
    }

    fun getImgArt(path:String):ByteArray?{
        val retriever=MediaMetadataRetriever()
        retriever.setDataSource(path)
        return retriever.embeddedPicture
    }
    fun createMediaPlayer() {
        try {
            if (mediaPlayer == null)
                mediaPlayer = MediaPlayer()
                mediaPlayer?.reset()

                mediaPlayer?.setDataSource(serviceList[position].songPath)
                lastPlayedSongId = serviceList[position].songId

            mediaPlayer?.prepare()
            mediaPlayer?.start()
//            lastPlayedSongId = serviceList[position].songId
//            isPlaying=true
        } catch (e: Exception) {
            Log.i("MusicSystems", e.toString())
            return
        }
    }

    fun songSelectionList(value: Int) {

        if (value == 1) {
            serviceList.clear()
            serviceList.addAll(allMusicList)
            Log.i("listsize", allMusicList.size.toString())
        } else if (value == 2) {
            serviceList.clear()
            serviceList.addAll(favouriteList)
        }
        else if(value == 3){
            serviceList.clear()
            serviceList.addAll(playlistSongs)
        }
    }

    fun setSongDetailsAtBottomSheet() {


        var uri = serviceList[position].imagePath
        var imageUri = Uri.parse(uri)

            mainFragment.get()?.let {it.mainFragmentBinding.bottomSheetSongName.text = serviceList[position].songName}
            mainFragment.get()?.let { it.mainFragmentBinding.bottomSheetduration.text = formatDuration(serviceList[position].duration) } //Todo
            mainFragment.get()?.mainFragmentBinding?.bottomSheetMusicThumbnail?.let {
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).fitCenter().into(
                it
            )
        }



        Log.i("position", serviceList[position].songName)

    }






    fun setSongPosition(value: Boolean) {
        if (value) {
            if (position == serviceList.size - 1) {
                position = 0
                Log.i("Music size", position.toString())
            } else {
                position = (position + 1) % serviceList.size
            }
            lastPlayedSongId = serviceList[position].songId

        } else {
            if (position == 0) {
                position = serviceList.size - 1
            } else {
                position = (position - 1) % serviceList.size
            }
            lastPlayedSongId = serviceList[position].songId
        }
    }


    inner class MyBinder : Binder() {

        fun currentBinder(): MusicServices? {
            return this@MusicServices
        }
    }
}