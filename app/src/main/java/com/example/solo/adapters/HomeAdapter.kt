package com.example.solo.adapters

import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.solo.sub_main_fragments.FavouriteFragment
import com.example.solo.sub_main_fragments.HomeFragment


class HomeAdapter(var fragmentManager: FragmentManager,var lifecycle: Lifecycle, var view: View):FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        Log.i("Viewpager","checking")
      return when(position){
          0 -> HomeFragment(view)
          else->FavouriteFragment(view)
       }
    }




    fun updateAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle) {
        this.fragmentManager=fragmentManager
        this.lifecycle=lifecycle

        notifyDataSetChanged()
    }



}