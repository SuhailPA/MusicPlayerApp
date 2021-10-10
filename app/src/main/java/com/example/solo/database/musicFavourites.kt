package com.example.solo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.solo.modelclass.MusicModelClass


@Database(entities = [MusicModelClass::class], version = 1, exportSchema = false)
abstract class musicFavourites : RoomDatabase() {

    abstract fun musicDao(): musicDao

    companion object {
        @Volatile
        var INSTANCE: musicFavourites? = null


        fun getDatabase(context: Context): musicFavourites {

            val instance = INSTANCE
            if (instance == null) {

                synchronized(this) {
                    val instance = Room.databaseBuilder(
                            context.applicationContext, musicFavourites::class.java,
                            "favourite_database1"
                    ).build()
                    INSTANCE = instance
                    return instance
                }
            }
            return instance
        }
    }
}