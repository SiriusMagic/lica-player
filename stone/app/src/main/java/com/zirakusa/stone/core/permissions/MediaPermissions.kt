package com.zirakusa.stone.core.permissions

import android.Manifest
import android.os.Build

/** The read permissions Sren needs to see the device's audio and video. */
object MediaPermissions {
    val required: List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
