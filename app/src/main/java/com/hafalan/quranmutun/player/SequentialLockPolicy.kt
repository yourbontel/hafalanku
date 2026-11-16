package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.model.AudioTargetType

object SequentialLockPolicy {

    fun orderedPlaylistForCategory(
        allAudios: List<DriveAudioEntity>,
        targetType: AudioTargetType,
        minHafalanSurah: Int,
        maxHafalanSurah: Int
    ): List<DriveAudioEntity> {
        return when (targetType) {
            AudioTargetType.QURAN -> allAudios
                .filter {
                    it.targetType == AudioTargetType.QURAN &&
                        it.surahNumber != null &&
                        it.surahNumber in minHafalanSurah..maxHafalanSurah
                }
                .sortedWith(compareBy<DriveAudioEntity> { it.surahNumber ?: Int.MAX_VALUE }.thenBy { it.id })
            AudioTargetType.MUTUN -> allAudios
                .filter { it.targetType == AudioTargetType.MUTUN }
                .sortedWith(compareBy<DriveAudioEntity> { it.mutunId ?: Long.MAX_VALUE }.thenBy { it.id })
            AudioTargetType.GENERAL -> allAudios
                .filter { it.targetType == AudioTargetType.GENERAL }
                .sortedBy { it.id }
        }
    }

    fun isUnlocked(
        orderedPlaylist: List<DriveAudioEntity>,
        index: Int,
        completedAudioIds: Set<Long>
    ): Boolean {
        if (index !in orderedPlaylist.indices) return false
        if (index == 0) return true
        return (0 until index).all { i -> orderedPlaylist[i].id in completedAudioIds }
    }

    fun firstPendingIndex(
        orderedPlaylist: List<DriveAudioEntity>,
        completedAudioIds: Set<Long>
    ): Int {
        if (orderedPlaylist.isEmpty()) return -1
        val pending = orderedPlaylist.indexOfFirst { it.id !in completedAudioIds }
        return if (pending >= 0) pending else 0
    }

    fun canAdvanceToNext(
        orderedPlaylist: List<DriveAudioEntity>,
        currentIndex: Int,
        completedAudioIds: Set<Long>
    ): Boolean {
        if (currentIndex !in 0 until orderedPlaylist.lastIndex) return false
        return isUnlocked(orderedPlaylist, currentIndex + 1, completedAudioIds)
    }

    fun canGoToPrevious(
        orderedPlaylist: List<DriveAudioEntity>,
        currentIndex: Int,
        completedAudioIds: Set<Long>
    ): Boolean {
        if (currentIndex <= 0 || currentIndex !in orderedPlaylist.indices) return false
        return isUnlocked(orderedPlaylist, currentIndex - 1, completedAudioIds)
    }

    fun shouldIncrementCycleOnCompletion(
        orderedPlaylist: List<DriveAudioEntity>,
        completedBefore: Set<Long>,
        justCompletedAudioId: Long
    ): Boolean {
        if (orderedPlaylist.isEmpty()) return false
        if (justCompletedAudioId in completedBefore) return false
        if (orderedPlaylist.none { it.id == justCompletedAudioId }) return false
        val completedAfter = completedBefore + justCompletedAudioId
        return orderedPlaylist.all { it.id in completedAfter }
    }
}
