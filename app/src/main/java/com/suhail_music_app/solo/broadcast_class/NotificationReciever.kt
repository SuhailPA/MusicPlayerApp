package com.suhail_music_app.solo.broadcast_class


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.modelclass.formatDuration
import com.suhail_music_app.solo.modelclass.musicServices
import com.suhail_music_app.solo.notification.ApplicationClass
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.system.exitProcess

class NotificationReciever : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {


        var state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
        when (intent.action) {
            ApplicationClass.PLAY -> {
                if (musicServices?.get().let { it?.mediaPlayer?.isPlaying }!!) pauseMusic()
                else playMusic()
            }
            ApplicationClass.NEXT -> playNextSong(true, context)

            ApplicationClass.PREVIOUS -> playNextSong(false, context)

            ApplicationClass.EXIT -> {

                if (musicServices?.get().let { it?.mediaPlayer!=null } && !musicServices?.get().let { it?.mainActivity?.get().let { it!!.isDestroyed } }){
                    pauseMusic()
                    musicServices?.get().let { it?.stopForeground(true) }

                } else if (musicServices?.get().let { it?.mediaPlayer!=null } && musicServices?.get().let { it?.mainActivity?.get().let { it!!.isDestroyed } }){
                    Log.i("Checkbrodcast","rewsdf")


                     musicServices?.get().let { it?.mediaPlayer?.pause() }
                     musicServices?.get().let { it?.mediaPlayer?.stop() }
                     musicServices?.get().let { it?.mediaPlayer?.release() }
                     musicServices?.get().let { it?.stopForeground(true) }
                    exitProcess(1)



//
                }
            }
        }
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            pauseMusic()
        }
        try {
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING )) {
                if (musicServices!=null && musicServices?.get().let { it?.mediaPlayer!=null }) pauseMusic()

            }
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (musicServices!=null && musicServices?.get().let { it?.mediaPlayer!=null }) pauseMusic()
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (musicServices!=null && musicServices?.get().let { it?.mediaPlayer!=null }) playMusic()
            }
        }catch (e:Exception){
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }
}

private fun playMusic() {


    musicServices?.get().let { it?.mediaPlayer?.start() }
    musicServices?.get().let { it?.showNotification(R.drawable.pause_button) }
    musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.playButton?.setImageResource(R.drawable.pause_button) } }
    musicServices?.get().let { it?.mainFragment?.get().let { it?.mainFragmentBinding?.bottomSheetPlayButton?.setImageResource(R.drawable.pause_button) }}

}

private fun pauseMusic() {

    musicServices?.get().let { it?.mediaPlayer?.pause() }
    musicServices?.get().let { it?.showNotification(R.drawable.play_button) }
    musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.playButton?.setImageResource(R.drawable.play_button) } }
    musicServices?.get().let { it?.mainFragment?.get().let { it?.mainFragmentBinding?.bottomSheetPlayButton?.setImageResource(R.drawable.play_button) } }
}

private fun playNextSong(value: Boolean, context: Context?) {

    musicServices?.get().let { it?.setSongPosition(value) }


        musicServices?.get().let { it?.createMediaPlayer() }
    var uri:String


        uri = musicServices?.get().let { it!!.serviceList[it.position].imagePath }
        musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.totalDuration?.text= formatDuration(musicServices?.get().let { it!!.serviceList[it.position].duration }) } }

        var imageUri = Uri.parse(uri)
        if (imageUri != null) {
            musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.backgroundImage }?.let {
                Glide.with(context!!).load(imageUri).placeholder(R.drawable.blurred_image).apply(
                        RequestOptions.bitmapTransform(
                                BlurTransformation(25)
                        )
                ).into(it)
            } }

            musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.mainMusicThumbnail }?.let {
                Glide.with(context!!).load(imageUri).placeholder(R.drawable.without_blur).into(
                        it
                )
            }
            } }
        musicServices?.get().let { it?.setSongDetailsAtBottomSheet() }
        musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.seekBar2?.progress=0 } }
        musicServices?.get().let { it?.musicActivity?.get().let { it?.musicBinding?.seekBar2?.max = (musicServices?.get().let { it?.mediaPlayer?.duration })?.div(1000)!! } }
        musicServices?.get().let { it?.showNotification(R.drawable.pause_button) }

}

