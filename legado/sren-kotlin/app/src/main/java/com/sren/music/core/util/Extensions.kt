package com.sren.music.core.util

import android.net.Uri
import java.io.File

fun String.toPlaybackUri(): Uri {
    return if (startsWith("content://")) Uri.parse(this) else Uri.fromFile(File(this))
}
