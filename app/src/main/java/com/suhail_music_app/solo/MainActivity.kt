package com.suhail_music_app.solo

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.suhail_music_app.solo.broadcast_class.NotificationReciever
import com.suhail_music_app.solo.databinding.ActivityMainBinding
import com.suhail_music_app.solo.modelclass.musicServices
import com.suhail_music_app.solo.services.MusicServices
import java.lang.ref.WeakReference
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(),ServiceConnection {
    lateinit var mainBinding: ActivityMainBinding
    val br: BroadcastReceiver = NotificationReciever()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MusicServices::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        startService(intent)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.myNavHostFragment) as NavHostFragment? ?: return

        val navController = host.navController

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.mainFragment)) //  IDs of fragments you want without the ActionBar home/up button

        var actionBar=supportActionBar
        actionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))


        setupActionBarWithNavController(navController, appBarConfiguration)


    }



    override fun onStop() {
        super.onStop()
        musicServices?.get().let { it?.isActive=false }
        if (musicServices!=null && !musicServices?.get().let { it?.mediaPlayer!!.isPlaying })
        {
            Log.i("Finishsed Everything","sdf")
            musicServices?.get().let { it?.mediaPlayer?.pause() }
            musicServices?.get().let { it?.stopForeground(true) }

        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicServices =WeakReference(binder.currentBinder())
        val weakMainActivityReference=WeakReference(this)
        musicServices!!.get().let { it?.mainActivity=weakMainActivityReference }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }
}