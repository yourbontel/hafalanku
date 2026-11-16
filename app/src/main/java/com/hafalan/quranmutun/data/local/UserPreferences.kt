package com.hafalan.quranmutun.data.local

import android.content.Context
import com.hafalan.quranmutun.data.model.AudioTargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ListeningPreferences(
    val minHafalanSurah: Int = 1,
    val maxHafalanSurah: Int = 9, // Default sampai At-Taubah (Surah 9)
    val autoAdvancePlaylist: Boolean = true,
    val keepScreenOnInDrivingMode: Boolean = false,
    val completedAudioIds: Set<Long> = emptySet(),
    val audioPlayCounts: Map<Long, Int> = emptyMap(),
    val quranCycleCount: Int = 0,
    val mutunCycleCount: Int = 0
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
        val playCounts = decodePlayCounts(prefs.getString(KEY_AUDIO_PLAY_COUNTS, "").orEmpty())
        return ListeningPreferences(
            minHafalanSurah = minSurah,
            maxHafalanSurah = maxSurah,
            autoAdvancePlaylist = prefs.getBoolean(KEY_AUTO_ADVANCE, true),
            keepScreenOnInDrivingMode = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false),
            completedAudioIds = completedIds,
            audioPlayCounts = playCounts,
            quranCycleCount = prefs.getInt(KEY_QURAN_CYCLE_COUNT, 0).coerceAtLeast(0),
            mutunCycleCount = prefs.getInt(KEY_MUTUN_CYCLE_COUNT, 0).coerceAtLeast(0)
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

    fun incrementAudioPlayCount(audioId: Long) {
        val currentMap = _state.value.audioPlayCounts.toMutableMap()
        currentMap[audioId] = (currentMap[audioId] ?: 0) + 1
        prefs.edit().putString(KEY_AUDIO_PLAY_COUNTS, encodePlayCounts(currentMap)).apply()
        _state.update { it.copy(audioPlayCounts = currentMap) }
    }

    fun markAudioCompleted(audioId: Long) {
        val updated = _state.value.completedAudioIds + audioId
        saveCompletedIds(updated)
    }

    fun incrementCycleCount(targetType: AudioTargetType) {
        when (targetType) {
            AudioTargetType.QURAN -> {
                val next = _state.value.quranCycleCount + 1
                prefs.edit().putInt(KEY_QURAN_CYCLE_COUNT, next).apply()
                _state.update { it.copy(quranCycleCount = next) }
            }
            AudioTargetType.MUTUN -> {
                val next = _state.value.mutunCycleCount + 1
                prefs.edit().putInt(KEY_MUTUN_CYCLE_COUNT, next).apply()
                _state.update { it.copy(mutunCycleCount = next) }
            }
            AudioTargetType.GENERAL -> Unit
        }
    }

    fun resetCompletedAudios(audioIdsToReset: Collection<Long>) {
        if (audioIdsToReset.isEmpty()) return
        val updated = _state.value.completedAudioIds - audioIdsToReset.toSet()
        saveCompletedIds(updated)
    }

    fun resetAllRepetitionStats() {
        prefs.edit()
            .putString(KEY_AUDIO_PLAY_COUNTS, "")
            .putInt(KEY_QURAN_CYCLE_COUNT, 0)
            .putInt(KEY_MUTUN_CYCLE_COUNT, 0)
            .apply()
        _state.update {
            it.copy(
                audioPlayCounts = emptyMap(),
                quranCycleCount = 0,
                mutunCycleCount = 0
            )
        }
    }

    private fun saveCompletedIds(ids: Set<Long>) {
        prefs.edit()
            .putStringSet(KEY_COMPLETED_AUDIO_IDS, ids.map { it.toString() }.toSet())
            .apply()
        _state.update { it.copy(completedAudioIds = ids) }
    }

    private fun encodePlayCounts(map: Map<Long, Int>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value}" }

    private fun decodePlayCounts(raw: String): Map<Long, Int> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()
                    val count = parts[1].toIntOrNull()
                    if (id != null && count != null && count > 0) id to count else null
                } else null
            }
            .toMap()
    }

    private companion object {
        const val KEY_MIN_SURAH = "min_hafalan_surah"
        const val KEY_MAX_SURAH = "max_hafalan_surah"
        const val KEY_AUTO_ADVANCE = "auto_advance_playlist"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on_driving"
        const val KEY_COMPLETED_AUDIO_IDS = "completed_audio_ids"
        const val KEY_AUDIO_PLAY_COUNTS = "audio_play_counts"
        const val KEY_QURAN_CYCLE_COUNT = "quran_cycle_count"
        const val KEY_MUTUN_CYCLE_COUNT = "mutun_cycle_count"
    }
}
