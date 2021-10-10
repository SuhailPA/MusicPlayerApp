package com.example.solo

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.solo.adapters.PlaylistsRecyclerAdapter
import com.example.solo.broadcast_class.NotificationReciever
import com.example.solo.databinding.ActivityMainBinding
import com.example.solo.modelclass.musicServices
import com.example.solo.services.MusicServices


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


    //    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//
//        when(keyCode){
//            KeyEvent.KEYCODE_VOLUME_DOWN->{
//                if (musicServices!=null && musicServices!!.isMute){
//                    musicServices!!.mediaPlayer!!.setVolume(1.0F,1.0F)
//                }
//
//            }
//            KeyEvent.KEYCODE_VOLUME_UP->{
//                if (musicServices!=null && musicServices!!.isMute){
//                    musicServices!!.mediaPlayer!!.setVolume(1.0F,1.0F)
//                }
//            }
//        }
//        return true
//    }

    override fun onStop() {
        super.onStop()
        musicServices!!.isActive=false

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicServices = binder.currentBinder()
        musicServices!!.mainActivity=this
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }


}