package com.example.solo.services

import android.app.*
import android.content.DialogInterface
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
import androidx.fragment.app.FragmentActivity
import androidx.media.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.solo.MainActivity
import com.example.solo.R
import com.example.solo.broadcast_class.NotificationReciever
import com.example.solo.database.musicFavourites
import com.example.solo.main_fragments.MainFragment
import com.example.solo.main_fragments.MainFragmentDirections
import com.example.solo.main_fragments.MusicFragment
import com.example.solo.modelclass.MusicModelClass
import com.example.solo.modelclass.formatDuration
import com.example.solo.modelclass.musicServices
import com.example.solo.notification.ApplicationClass
import com.example.solo.sub_main_fragments.FavouriteFragment
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*


class MusicServices : Service() {

    var lastPlayedSongId: Int = -1
    lateinit var mainActivity:Activity
     var serviceList = mutableListOf<MusicModelClass>()
    lateinit var allMusicList :List<MusicModelClass>
     var timerValueInSec:Long=0L
    lateinit var favouriteList: List<MusicModelClass>
    var isActive=true
    lateinit var playlistSongs:List<MusicModelClass>
    var position = -1
    var isTimer=false
    private lateinit var runnable: Runnable
    var mediaPlayer: MediaPlayer? = null
    private var mybinder = MyBinder()
    lateinit var musicActivity: MusicFragment
    lateinit var mainFragment: MainFragment
    lateinit var favFragment:FavouriteFragment
    lateinit var mediaSession: MediaSessionCompat
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")

        return mybinder
    }

    fun navigationFromMainFragmentToPlaylistFragment(playlistName: String){
        var navController: NavController = Navigation.findNavController(mainFragment.mainView)
        var actions= MainFragmentDirections.actionMainFragmentToPlaylistsFragment(playlistName)
        navController.navigate(actions)
    }



    fun playMusicSystem() {
        Log.i("PlayMusic", "song")
        musicActivity.musicBinding.playButton.setImageResource(R.drawable.pause_button)
//        isPlaying = true
        musicServices!!.mediaPlayer!!.start()

        musicServices!!.showNotification(R.drawable.pause_button)
    }






    fun pauseMusicSystem() {
        musicActivity.musicBinding.playButton.setImageResource(R.drawable.play_button)
//        isPlaying = false
        musicServices!!.mediaPlayer!!.pause()
        musicServices!!.showNotification(R.drawable.play_button)
    }

    fun methodForPreviousAndNext(value: Boolean) {

        musicServices!!.setSongPosition(value)
        setLayout(musicServices!!.position)
        setSongDetailsAtBottomSheet()
        musicActivity.createMediaPlayer()
        lastPlayedSongId= serviceList[position].songId
    }

    fun timerServices(timerValue: Long, fragmentActivity: FragmentActivity){
        Thread{Thread.sleep(timerValue)
            Log.i("ThreadTest", "working")
            if (isTimer && timerValueInSec==timerValue){
                isTimer=false
                if (mediaPlayer!=null && mainActivity.isDestroyed){
                    musicServices!!.mediaPlayer!!.pause()
                    musicServices!!.mediaPlayer!!.stop()
                    musicServices!!.stopForeground(true)
                    musicServices!!.mediaPlayer!!.release()
                }else {
                    if (musicActivity.isVisible) {
                        mainActivity.runOnUiThread {
                            updateDrawableImage()
                        }
                    }
                    mainActivity.runOnUiThread {
                        Toast.makeText(mainActivity,"Since the application is not destroyed the timer will be disabled",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()

    }

    private fun updateDrawableImage() {
        musicActivity.musicBinding.timerSet.setImageResource(R.drawable.timer_off)
    }


    fun setLayout(position: Int) {
      var uri:String


            uri = musicServices!!.serviceList[position].imagePath
            musicActivity.musicBinding.totalDuration.text = formatDuration(musicServices!!.serviceList[position].duration)


        var imageUri = Uri.parse(uri)
        Log.i("1234Fragment", imageUri.toString())
        if (imageUri != null) {
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail_blurred).apply(
                    RequestOptions.bitmapTransform(
                            BlurTransformation(25))
            ).into(musicActivity.musicBinding.backgroundImage)
            Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).into(musicActivity.musicBinding.mainMusicThumbnail)
        }

    }


    fun seekBarSetup(activity: MusicFragment) {
        this.musicActivity = activity
        runnable = Runnable {
            musicActivity.musicBinding.timeCount.text = formatDuration(musicServices!!.mediaPlayer!!.currentPosition.toLong())
            musicActivity.musicBinding.seekBar2.progress = musicServices!!.mediaPlayer!!.currentPosition / 1000
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    fun getAllSongs() {
        val stdDao = musicFavourites.getDatabase(application).musicDao()
        GlobalScope.launch(Dispatchers.IO) {
            musicServices!!.favouriteList = emptyList()
            musicServices!!.favouriteList = stdDao.readAllMusic()
        }
    }

    fun getPlaylistSongs(name: String) {
        val stdDao = musicFavourites.getDatabase(application).musicDao()
            musicServices!!.playlistSongs = emptyList()
            musicServices!!.playlistSongs = stdDao.readSongsFromPlaylist(name)
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


        val songThumb=getImgArt(allMusicList[position].songPath)
        val image = if (songThumb!=null) {
            BitmapFactory.decodeByteArray(songThumb, 0, songThumb.size)
        }else{
                BitmapFactory.decodeResource(resources,R.drawable.music_thumbnail_blurred)
            }


        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL1)
                .setContentIntent(pendingIntent)
                .setContentTitle(serviceList[position].songName)
                .setContentText(serviceList[position].artistName)
                .setSmallIcon(R.drawable.without_blur)
                .setLargeIcon(image)
                .setStyle(NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_baseline_close_24, "Exit", exitPendingIntent)
                .addAction(R.drawable.previous_button, "Previous", prevPendingIntent)
                .addAction(playPauseButton, "Play", playPendingIntent)
                .addAction(R.drawable.next_black, "Next", nextPendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (musicServices!!.mediaPlayer!!.isPlaying) 1F else 0F
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
                    val playBackStateNew = PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .build()
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
            if (musicServices!!.mediaPlayer == null)
                musicServices!!.mediaPlayer = MediaPlayer()
            musicServices!!.mediaPlayer!!.reset()
                musicServices!!.mediaPlayer!!.setDataSource(serviceList[position].songPath)
                lastPlayedSongId = serviceList[position].songId

            musicServices!!.mediaPlayer!!.prepare()
            musicServices!!.mediaPlayer!!.start()
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


        var uri = musicServices!!.serviceList[position].imagePath
        var imageUri = Uri.parse(uri)

            mainFragment.mainFragmentBinding.bottomSheetSongName.text = musicServices!!.serviceList[musicServices!!.position].songName
            mainFragment.mainFragmentBinding.bottomSheetduration.text = formatDuration(musicServices!!.serviceList[musicServices!!.position].duration)
        Glide.with(this).load(imageUri).placeholder(R.drawable.music_thumbnail).fitCenter().into(mainFragment.mainFragmentBinding.bottomSheetMusicThumbnail)



        Log.i("position", serviceList[musicServices!!.position].songName)

    }


    fun setFragment(activity: MainFragment) {
        this.mainFragment = activity
    }


//    fun shuffledSongPosition(value: Boolean){
//        if (value){
//            if (shuffledLists.isEmpty()){
//                isShuffled=false
//                position=0
//            }else{
//                positionsList.add(position)
//                shuffledLists.removeAt(position)
//                if (shuffledLists.isNotEmpty()) position =Random().nextInt(shuffledLists.size)
//                else {
//                    isShuffled=false
//                    position=0
//                }
//
//            }
//        }else{
//            isShuffled=false
//            position = position
//        }
//    }

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