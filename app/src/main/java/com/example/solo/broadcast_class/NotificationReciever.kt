package com.example.solo.broadcast_class


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.telephony.TelephonyManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.solo.R
import com.example.solo.modelclass.formatDuration
import com.example.solo.modelclass.musicServices
import com.example.solo.notification.ApplicationClass
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.system.exitProcess

class NotificationReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
        when (intent?.action) {
            ApplicationClass.PLAY -> {
                if (musicServices!!.mediaPlayer!!.isPlaying) pauseMusic()
                else playMusic()
            }
            ApplicationClass.NEXT -> playNextSong(true, context)

            ApplicationClass.PREVIOUS -> playNextSong(false, context)

            ApplicationClass.EXIT -> {

                if (musicServices!!.mediaPlayer != null && !musicServices!!.mainActivity.isDestroyed) {
                    pauseMusic()
                    musicServices!!.stopForeground(true)

                } else if (musicServices!!.mediaPlayer != null && musicServices!!.mainActivity.isDestroyed) {
                    musicServices!!.mediaPlayer!!.pause()
                    musicServices!!.mediaPlayer!!.stop()
                    musicServices!!.mediaPlayer!!.release()
                    musicServices!!.stopForeground(true)
//
                }
            }
        }
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            pauseMusic()
        }
        try {
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING )) {
                if (musicServices!=null && musicServices!!.mediaPlayer!=null) pauseMusic()

            }
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (musicServices!=null && musicServices!!.mediaPlayer!=null) pauseMusic()
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (musicServices!=null && musicServices!!.mediaPlayer!=null) playMusic()
            }
        }catch (e:Exception){
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }
}

private fun playMusic() {

    musicServices!!.mediaPlayer!!.start()
    musicServices!!.showNotification(R.drawable.pause_button)
    musicServices!!.musicActivity.musicBinding.playButton.setImageResource(R.drawable.pause_button)
    musicServices!!.mainFragment.mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.pause_button)

}

private fun pauseMusic() {

    musicServices!!.mediaPlayer!!.pause()
    musicServices!!.showNotification(R.drawable.play_button)
    musicServices!!.musicActivity.musicBinding.playButton.setImageResource(R.drawable.play_button)
    musicServices!!.mainFragment.mainFragmentBinding.bottomSheetPlayButton.setImageResource(R.drawable.play_button)
}

private fun playNextSong(value: Boolean, context: Context?) {

    musicServices!!.setSongPosition(value)


        musicServices!!.createMediaPlayer()
    var uri:String


        uri = musicServices!!.serviceList[musicServices!!.position].imagePath
        musicServices!!.musicActivity.musicBinding.totalDuration.text = formatDuration(musicServices!!.serviceList[musicServices!!.position].duration)

        var imageUri = Uri.parse(uri)
        if (imageUri != null) {
            Glide.with(context!!).load(imageUri).placeholder(R.drawable.blurred_image).apply(
                RequestOptions.bitmapTransform(
                    BlurTransformation(25)
                )
            ).into(musicServices!!.musicActivity.musicBinding.backgroundImage)
            Glide.with(context).load(imageUri).placeholder(R.drawable.without_blur).into(musicServices!!.musicActivity.musicBinding.mainMusicThumbnail)
        }
        musicServices!!.setSongDetailsAtBottomSheet()
        musicServices!!.musicActivity.musicBinding.seekBar2.progress = 0
        musicServices!!.musicActivity.musicBinding.seekBar2.max = (musicServices!!.mediaPlayer!!.duration) / 1000
        musicServices!!.showNotification(R.drawable.pause_button)

}

