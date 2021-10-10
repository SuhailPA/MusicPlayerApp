package com.example.solo.database

import androidx.room.*
import com.example.solo.modelclass.MusicModelClass


@Dao
interface musicDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
     fun addMusic(musicItem:MusicModelClass)

    @Query("SELECT * FROM `database` WHERE `playlist-name` LIKE 'favourites'")
    fun readAllMusic():List<MusicModelClass>

    @Query("SELECT DISTINCT `playlist-name` FROM `database` ")
    fun readAllFiles():List<String>

    @Query("SELECT * FROM `database` WHERE `playlist-name` LIKE :name ")
    fun readSongsFromPlaylist(name:String):MutableList<MusicModelClass>

    @Query("SELECT * FROM `database` WHERE `song-Id` LIKE :songId AND `playlist-name` LIKE :name")
    fun checkForRepeatedSong(songId:Int,name: String):Boolean

    @Query("DELETE  FROM `database` WHERE `playlist-name` LIKE :name")
    suspend fun deletePlaylist(name:String)

    @Delete
    fun deleteSong(song:MusicModelClass)
}