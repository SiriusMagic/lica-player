package com.sren.music.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A video file from the device's MediaStore. */
@Entity(tableName = "videos")
data class Video(
    @PrimaryKey val id: Long,
    val title: String,
    val duration: Long,         // Milliseconds
    val path: String,           // content:// URI string
    val size: Long,             // Bytes
    val width: Int,
    val height: Int,
    val dateAdded: Long,
)
