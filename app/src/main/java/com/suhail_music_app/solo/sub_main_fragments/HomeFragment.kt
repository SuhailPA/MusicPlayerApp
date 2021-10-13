package com.suhail_music_app.solo.sub_main_fragments

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suhail_music_app.solo.R
import com.suhail_music_app.solo.adapters.HomeAdapter
import com.suhail_music_app.solo.adapters.MusicRecyclerAdapter
import com.suhail_music_app.solo.adapters.PlaylistNameRecommandationAdapter
import com.suhail_music_app.solo.adapters.PlaylistsRecyclerAdapter
import com.suhail_music_app.solo.database.musicDao
import com.suhail_music_app.solo.database.musicFavourites
import com.suhail_music_app.solo.databinding.FragmentHomeBinding
import com.suhail_music_app.solo.main_fragments.MainFragmentDirections
import com.suhail_music_app.solo.modelclass.MusicModelClass
import com.suhail_music_app.solo.modelclass.musicServices
import com.suhail_music_app.solo.modelclass.playlistFiles
import com.suhail_music_app.solo.services.MusicServices
import kotlinx.android.synthetic.main.alert_box_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


class HomeFragment(var mainview: WeakReference<View>) : Fragment(), ServiceConnection {
//Original with Read Problem


    var adapterS: MusicRecyclerAdapter? = null
    var adapterF:PlaylistsRecyclerAdapter?=null
    lateinit var homeAdapter:HomeAdapter
    lateinit var musicFavDao : musicDao
    var checkPlaylistNames= mutableListOf<String>()
    companion object {
        var musicList = mutableListOf<MusicModelClass>()

    }
    lateinit var navController: NavController

    private val permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {


        if (!it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && !it[Manifest.permission.READ_PHONE_STATE]!!){
            showMessage(3)
        }else if(!it[Manifest.permission.READ_EXTERNAL_STORAGE]!!){
            showMessage(1)
        }else if (!it[Manifest.permission.READ_PHONE_STATE]!!){
            val intent = Intent(activity, MusicServices::class.java)
            activity?.bindService(intent, this@HomeFragment, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)
            readAllMusics()
        }else{
            val intent = Intent(activity, MusicServices::class.java)
            activity?.bindService(intent, this@HomeFragment, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)
            readAllMusics()
        }

    }


    private fun showMessage(perValue:Int)
    {
        var message: String

        if(perValue==1){
            message="Read media file permission is required to read all the songs"
        }else if (perValue==2){
            message="Phone state permission may be required in order to control the music system while calling"
        }else{
            message="Read media file permission && Phone state permission may be required in order to control the music system while calling"
        }
        AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, _ ->
                    if (perValue!=2){
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package",requireActivity().packageName, null)
                        intent.data = uri
                        resultLauncher.launch(intent)
                    }
                    else{
                        dialog.dismiss()
                    }

                })
                .create()

                .show()

    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
           
        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            val intent = Intent(activity, MusicServices::class.java)
            activity?.bindService(intent, this@HomeFragment, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)
            readAllMusics()
        }else{
            showMessage(1)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    lateinit var bindingOffline: FragmentHomeBinding
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.i("NumberOfExecution", "home1")
        Log.i("onCreateViewWorking", "home")
        Log.i("Lifecycle", "Created Home")
        bindingOffline = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)




        navController = mainview.get()?.let { Navigation.findNavController(it) }!!
        musicFavDao = musicFavourites.getDatabase(requireActivity().application).musicDao()
        adapterS = activity?.let { mainview.get()?.let { it1 -> MusicRecyclerAdapter(musicList, it, it1, 1, onClicked, popupOnClick) } }


        permission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE))


        return bindingOffline.root
    }




    private var popupOnClick: (View, Int, MutableList<MusicModelClass>)->Unit={ view: View, i: Int, musicLists: MutableList<MusicModelClass> ->
        showPopup(view, i, musicLists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapterS?.onDetachedFromRecyclerView(bindingOffline.musicRecyclerview)
    }

    fun showPopup(mview: View, position: Int, items: MutableList<MusicModelClass>){
        val popUp= PopupMenu(requireContext(), mview)
        popUp.inflate(R.menu.options_menu)

        var item=popUp.menu.findItem(R.id.removeFromPlaylist)
        item.isVisible = false


        popUp.setOnMenuItemClickListener {

            when (it.itemId){
                R.id.addToPlayList -> {
                    alertBoxForPlaylist( items[position])
                    true
                }

                else ->   {
                    true
                }

            }
        }
        popUp.show()

    }

    private fun alertBoxForPlaylist( item: MusicModelClass) {

        val alertBox = LayoutInflater.from(requireActivity()).inflate(R.layout.alert_box_layout, null)
        val alertBoxBuilder = AlertDialog.Builder(requireActivity()).setView(alertBox)
        alertBoxBuilder.setCancelable(false)


        adapterF = PlaylistsRecyclerAdapter(playlistFiles, null)




        GlobalScope.launch(Dispatchers.IO){

            var namelists = musicFavDao.readAllFiles() as MutableList<String>
            withContext(Dispatchers.Main){

                if(namelists.contains("favourites"))namelists.remove("favourites")
                var adapterPF=PlaylistNameRecommandationAdapter(namelists,alertBox.playListNameEditText)

                var recyclerView=alertBox.findViewById<RecyclerView>(R.id.listNameRecycler)

                if(namelists.isNotEmpty()){
                    recyclerView.visibility=View.VISIBLE
                }
                recyclerView.apply {
                    this.adapter=adapterPF
                    this.layoutManager=GridLayoutManager(requireActivity(),2)
                }





                val alertBoxInstance = alertBoxBuilder.show()
                alertBox.alertBoxOk.setOnClickListener {
                    val playlistName = alertBox.playListNameEditText.text.toString()
                    if (!namelists.contains(playlistName)) {
                        playlistFiles.add(playlistName)

                        adapterF!!.updateAdapter(playlistFiles)
            
                       
                    }
                    createPlaylistName(playlistName, item)
                    alertBoxInstance.dismiss()
                }
                alertBox.alertBoxCancel.setOnClickListener {
                    alertBoxInstance.dismiss()
                }
            }
        }
    }

    private fun createPlaylistName(playlistName: String, item: MusicModelClass) {
//        adapterP!!.notifyDataSetChanged()
        item.playlistName=playlistName
        item.timeStamp=System.currentTimeMillis().toString()+item.songId.toString()

        try {
            GlobalScope.launch(Dispatchers.IO) {
                if (!musicFavDao.checkForRepeatedSong(item.songId, playlistName)){
                    musicFavDao.addMusic(item)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(
                                context,
                                "Selected song is already in the playlist",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }catch (e: Exception){
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }





    }




    private val onClicked: (MusicModelClass, Int)->Unit={ musicModelClass: MusicModelClass, i: Int ->

        var actions: NavDirections =
                MainFragmentDirections.actionMainFragmentToMusicFragment(i, musicModelClass.songId)
        navController.navigate(actions)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            super.onCreateOptionsMenu(menu, inflater)
            inflater.inflate(R.menu.menu_options_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ascending -> ascendingOrderedSort()
            R.id.descending -> descendingOrderedSort()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun ascendingOrderedSort(){
        var sortedList= mutableListOf<MusicModelClass>()
        if ( sortedList.isNotEmpty())sortedList.clear()
        sortedList.addAll(musicList)
        sortedList.sortBy { it.songName }
        musicServices!!.get().let { it?.allMusicList=sortedList }
        Log.i("lisstCheckS", sortedList[0].songName)
        adapterS = activity?.let { mainview.get()?.let { it1 -> MusicRecyclerAdapter(sortedList, it, it1, 1, onClicked, popupOnClick) } }
        bindingOffline.musicRecyclerview.apply {
            adapter = adapterS
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun descendingOrderedSort(){
        var discendingOrderedList= mutableListOf<MusicModelClass>()

        if (discendingOrderedList.isNotEmpty()) discendingOrderedList.clear()
        discendingOrderedList.addAll(musicList)
        discendingOrderedList.sortByDescending { it.songName }
        musicServices!!.get().let { it?.allMusicList=discendingOrderedList}
//        Log.i("lisstCheckD",discendingOrderedList[0].songName)
        adapterS = activity?.let { mainview.get()?.let { it1 -> MusicRecyclerAdapter(discendingOrderedList, it, it1, 1, onClicked, popupOnClick) } }
        bindingOffline.musicRecyclerview.apply {
            adapter = adapterS
            layoutManager = LinearLayoutManager(activity)
        }
    }


    override fun onStop() {
        super.onStop()
        Log.i("ResumeTestHome", "Stop")
    }

    fun readAllMusics() {
        Log.i("Lifecycle", "REadAllFiles")
        musicList.clear()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
        )

        val cursur: Cursor? = activity?.contentResolver?.query(
                uri,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE
        )

        bindingOffline.musicRecyclerview.apply {
            adapter = adapterS
            layoutManager = LinearLayoutManager(activity)
        }
        if (cursur != null) {
            while (cursur.moveToNext()) {
                val songId = cursur.getInt(0)
                val songName = cursur.getString(1)
                val artistName = cursur.getString(2)
                val duration = cursur.getLong(3)
                val songPath = cursur.getString(5)
                val albumId = cursur.getLong(4)
                var imagepath: Uri = Uri.parse("content://media/external/audio/albumart")
                var imageUri = ContentUris.withAppendedId(imagepath, albumId)
                val timeStamp: String = (System.currentTimeMillis() + songId).toString()
                val musicModel = MusicModelClass(
                        timeStamp,
                        songId,
                        "all",
                        songName,
                        artistName,
                        duration,
                        imageUri.toString(),
                        songPath
                )
                Log.i("Working","ii")
                musicList.add(musicModel)
                adapterS!!.notifyDataSetChanged()
            }
        }


//        bindingOffline.bubbleRecyclerView.attachToRecyclerView(bindingOffline.musicRecyclerview)
//
//
//        bindingOffline.bubbleRecyclerView.bubbleTextProvider = BubbleTextProvider { position ->
//            StringBuilder(musicList[position].songName.substring(0, 1)).toString()
//        }


    }



    override fun onResume() {
        super.onResume()
        Log.i("PositionCheck", "REsumeCheck")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicServices = WeakReference(binder.currentBinder())
        musicServices!!.get().let { it?.allMusicList= musicList }

        Log.i("TestWork", musicServices!!.get().let { it?.allMusicList?.size.toString() })
        Log.i("NumberOfExecution", "home3")
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }



}