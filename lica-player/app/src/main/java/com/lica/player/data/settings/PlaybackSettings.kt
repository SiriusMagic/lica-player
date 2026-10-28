package com.lica.player.data.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Playback preferences. SharedPreferences (no DataStore dependency): one small
 * XML file, read once at startup. Shared by the UI and the playback service,
 * which run in the same process, so one in-memory [StateFlow] is enough.
 */
@Singleton
class PlaybackSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("playback_settings", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<AudioProfile> = _profile.asStateFlow()

    fun setProfile(profile: AudioProfile) {
        if (_profile.value == profile) return
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
        _profile.value = profile
    }

    private fun readProfile(): AudioProfile =
        prefs.getString(KEY_PROFILE, null)
            ?.let { name -> AudioProfile.entries.firstOrNull { it.name == name } }
            ?: AudioProfile.DEFAULT

    private companion object {
        const val KEY_PROFILE = "audio_profile"
    }
}
