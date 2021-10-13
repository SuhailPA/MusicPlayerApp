package com.suhail_music_app.solo.adapters

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.suhail_music_app.solo.sub_main_fragments.FavouriteFragment
import com.suhail_music_app.solo.sub_main_fragments.HomeFragment
import java.lang.ref.WeakReference


class HomeAdapter(var fragmentManager: FragmentManager,var lifecycle: Lifecycle, var view: View):FragmentStateAdapter(fragmentManager, lifecycle) {
    val weakView=WeakReference(view)
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        Log.i("Viewpager","checking")
      return when(position){
          0 -> HomeFragment(weakView)
          else->FavouriteFragment(view)
       }
    }




    fun updateAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle) {
        this.fragmentManager=fragmentManager
        this.lifecycle=lifecycle

        notifyDataSetChanged()
    }



}