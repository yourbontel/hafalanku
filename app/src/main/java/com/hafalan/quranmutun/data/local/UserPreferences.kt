package com.hafalan.quranmutun.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ListeningPreferences(
    val minHafalanSurah: Int = 1,
    val maxHafalanSurah: Int = 9, // Default sampai At-Taubah (Surah 9)
    val autoAdvancePlaylist: Boolean = true,
    val keepScreenOnInDrivingMode: Boolean = false,
    val completedAudioIds: Set<Long> = emptySet()
)

class UserPreferences(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("hafalan_listening_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(load())
    val state: StateFlow<ListeningPreferences> = _state.asStateFlow()

    private fun load(): ListeningPreferences {
        val minSurah = prefs.getInt(KEY_MIN_SURAH, 1).coerceIn(1, 114)
        val maxSurah = prefs.getInt(KEY_MAX_SURAH, 9).coerceIn(minSurah, 114)
        val completedRaw = prefs.getStringSet(KEY_COMPLETED_AUDIO_IDS, emptySet()).orEmpty()
        val completedIds = completedRaw.mapNotNull { it.toLongOrNull() }.toSet()
        return ListeningPreferences(
            minHafalanSurah = minSurah,
            maxHafalanSurah = maxSurah,
            autoAdvancePlaylist = prefs.getBoolean(KEY_AUTO_ADVANCE, true),
            keepScreenOnInDrivingMode = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false),
            completedAudioIds = completedIds
        )
    }

    fun setHafalanRange(minSurah: Int, maxSurah: Int) {
        val safeMin = minSurah.coerceIn(1, 114)
        val safeMax = maxSurah.coerceIn(safeMin, 114)
        prefs.edit()
            .putInt(KEY_MIN_SURAH, safeMin)
            .putInt(KEY_MAX_SURAH, safeMax)
            .apply()
        _state.update { it.copy(minHafalanSurah = safeMin, maxHafalanSurah = safeMax) }
    }

    fun setAutoAdvancePlaylist(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ADVANCE, enabled).apply()
        _state.update { it.copy(autoAdvancePlaylist = enabled) }
    }

    fun setKeepScreenOnInDrivingMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _state.update { it.copy(keepScreenOnInDrivingMode = enabled) }
    }

    fun markAudioCompleted(audioId: Long) {
        val updated = _state.value.completedAudioIds + audioId
        saveCompletedIds(updated)
    }

    fun resetCompletedAudios(audioIdsToReset: Collection<Long>) {
        if (audioIdsToReset.isEmpty()) return
        val updated = _state.value.completedAudioIds - audioIdsToReset.toSet()
        saveCompletedIds(updated)
    }

    private fun saveCompletedIds(ids: Set<Long>) {
        prefs.edit()
            .putStringSet(KEY_COMPLETED_AUDIO_IDS, ids.map { it.toString() }.toSet())
            .apply()
        _state.update { it.copy(completedAudioIds = ids) }
    }

    private companion object {
        const val KEY_MIN_SURAH = "min_hafalan_surah"
        const val KEY_MAX_SURAH = "max_hafalan_surah"
        const val KEY_AUTO_ADVANCE = "auto_advance_playlist"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on_driving"
        const val KEY_COMPLETED_AUDIO_IDS = "completed_audio_ids"
    }
}
