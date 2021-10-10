package com.example.solo.modelclass

import android.media.MediaPlayer
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solo.adapters.PlaylistsRecyclerAdapter
import com.example.solo.main_fragments.MusicFragment
import com.example.solo.services.MusicServices
import kotlinx.android.parcel.Parcelize
import java.time.Duration
import java.util.concurrent.TimeUnit

@Parcelize
@Entity(tableName = "database")
data class MusicModelClass(@PrimaryKey var timeStamp:String,
                           @ColumnInfo(name = "song-Id") val songId: Int,
                           @ColumnInfo(name = "playlist-name") var playlistName: String,
                           @ColumnInfo(name = "song_name") val songName: String,
                           @ColumnInfo(name = "artist_name") val artistName: String,
                           @ColumnInfo(name = "song_duration") val duration: Long,
                           @ColumnInfo(name = "song_image") val imagePath: String,
                           @ColumnInfo(name = "song_path") val songPath: String
) : Parcelable

var playlistFiles= mutableListOf<String>()
var musicServices: MusicServices? = null
fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%2d:%2d", minutes, seconds)
}


