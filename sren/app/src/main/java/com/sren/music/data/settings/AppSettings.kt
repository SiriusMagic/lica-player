package com.sren.music.data.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User preferences. SharedPreferences (no DataStore dependency): one small
 * XML file, read once at startup. Shared by the UI, the playback service and
 * the suggestion receiver, which all run in the same process.
 */
@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("playback_settings", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<AudioProfile> = _profile.asStateFlow()

    private val _songSort = MutableStateFlow(
        prefs.getString(KEY_SONG_SORT, null)
            ?.let { name -> SongSort.entries.firstOrNull { it.name == name } }
            ?: SongSort.DEFAULT,
    )
    val songSort: StateFlow<SongSort> = _songSort.asStateFlow()

    fun setSongSort(sort: SongSort) {
        prefs.edit().putString(KEY_SONG_SORT, sort.name).apply()
        _songSort.value = sort
    }

    private val _dailySuggestion = MutableStateFlow(prefs.getBoolean(KEY_DAILY_SUGGESTION, true))
    val dailySuggestion: StateFlow<Boolean> = _dailySuggestion.asStateFlow()

    /**
     * Gapless playback override: when true, forces offload with gapless regardless of the
     * audio profile. PlaybackService reads this to decide the gapless flag.
     */
    private val _gapless = MutableStateFlow(prefs.getBoolean(KEY_GAPLESS, false))
    val gapless: StateFlow<Boolean> = _gapless.asStateFlow()

    fun setGapless(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GAPLESS, enabled).apply()
        _gapless.value = enabled
    }

    /**
     * Default playback speed. Stored as a Float; the player reads this on start.
     * Valid values: 0.5, 0.75, 1.0, 1.25, 1.5, 2.0.
     */
    private val _defaultSpeed = MutableStateFlow(prefs.getFloat(KEY_DEFAULT_SPEED, 1f))
    val defaultSpeed: StateFlow<Float> = _defaultSpeed.asStateFlow()

    fun setDefaultSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_SPEED, speed).apply()
        _defaultSpeed.value = speed
    }

    /** The last song suggested, so the next day suggests a different one. */
    var lastSuggestedSongId: Long
        get() = prefs.getLong(KEY_LAST_SUGGESTION, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_SUGGESTION, value).apply()

    /** Android 13+: the notification permission is requested once, on first launch. */
    var notificationPermissionAsked: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_ASKED, value).apply()

    fun setProfile(profile: AudioProfile) {
        if (_profile.value == profile) return
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
        _profile.value = profile
    }

    fun setDailySuggestion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_SUGGESTION, enabled).apply()
        _dailySuggestion.value = enabled
    }

    private fun readProfile(): AudioProfile =
        prefs.getString(KEY_PROFILE, null)
            ?.let { name -> AudioProfile.entries.firstOrNull { it.name == name } }
            ?: AudioProfile.DEFAULT

    private companion object {
        const val KEY_PROFILE = "audio_profile"
        const val KEY_DAILY_SUGGESTION = "daily_suggestion"
        const val KEY_LAST_SUGGESTION = "last_suggested_song"
        const val KEY_SONG_SORT = "song_sort"
        const val KEY_NOTIF_ASKED = "notification_permission_asked"
        const val KEY_GAPLESS = "gapless_playback"
        const val KEY_DEFAULT_SPEED = "default_speed"
    }
}
