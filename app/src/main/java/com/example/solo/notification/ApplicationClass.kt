package com.example.solo.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.os.Build
import com.gu.toolargetool.TooLargeTool


class ApplicationClass:Application() {

    companion object{
        const val CHANNEL1="Channel1"
        const val PLAY="play"
        const val NEXT="next"
        const val PREVIOUS="previous"
        const val EXIT="exit"
    }
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            var notificationChannel=NotificationChannel(CHANNEL1,"Now playing", IMPORTANCE_LOW)
            notificationChannel.description="This is important channel for showing song"
            val notificationManager=getSystemService(NOTIFICATION_SERVICE)as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            TooLargeTool.startLogging(this)
        }

    }
}