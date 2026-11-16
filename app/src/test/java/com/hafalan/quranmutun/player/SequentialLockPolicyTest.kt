package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SequentialLockPolicyTest {

    private fun quranAudio(id: Long, surah: Int) = DriveAudioEntity(
        id = id,
        title = "Surah $surah",
        originalUrl = "https://drive.google.com/file/d/id_$id/view",
        driveFileId = "id_$id",
        streamUrl = "https://drive.google.com/uc?export=download&id=id_$id",
        targetType = AudioTargetType.QURAN,
        surahNumber = surah
    )

    private fun mutunAudio(id: Long, mutunId: Long) = DriveAudioEntity(
        id = id,
        title = "Mutun $mutunId",
        originalUrl = "https://drive.google.com/file/d/id_$id/view",
        driveFileId = "id_$id",
        streamUrl = "https://drive.google.com/uc?export=download&id=id_$id",
        targetType = AudioTargetType.MUTUN,
        mutunId = mutunId
    )

    @Test
    fun `quran playlist enforces sequential completion starting from min surah`() {
        val baqarah = quranAudio(10L, 2)
        val aliImran = quranAudio(11L, 3)
        val anNisa = quranAudio(12L, 4)
        val alFatihah = quranAudio(9L, 1)

        val ordered = SequentialLockPolicy.orderedPlaylistForCategory(
            allAudios = listOf(anNisa, baqarah, alFatihah, aliImran),
            targetType = AudioTargetType.QURAN,
            minHafalanSurah = 2,
            maxHafalanSurah = 9
        )

        // Starts at Al-Baqarah (Surah 2) and excludes Al-Fatihah (Surah 1)
        assertEquals(listOf(10L, 11L, 12L), ordered.map { it.id })

        // Nothing completed: only Al-Baqarah (index 0) is unlocked
        assertTrue(SequentialLockPolicy.isUnlocked(ordered, 0, emptySet()))
        assertFalse(SequentialLockPolicy.isUnlocked(ordered, 1, emptySet()))
        assertFalse(SequentialLockPolicy.canAdvanceToNext(ordered, 0, emptySet()))

        // After Al-Baqarah completes: Ali 'Imran (index 1) unlocks, An-Nisa still locked
        val afterBaqarah = setOf(10L)
        assertTrue(SequentialLockPolicy.isUnlocked(ordered, 1, afterBaqarah))
        assertTrue(SequentialLockPolicy.canAdvanceToNext(ordered, 0, afterBaqarah))
        assertFalse(SequentialLockPolicy.isUnlocked(ordered, 2, afterBaqarah))
        assertEquals(1, SequentialLockPolicy.firstPendingIndex(ordered, afterBaqarah))
    }

    @Test
    fun `mutun playlist unlocks independently from quran`() {
        val m1 = mutunAudio(20L, 1L)
        val m2 = mutunAudio(21L, 2L)
        val q1 = quranAudio(10L, 2)

        val orderedMutun = SequentialLockPolicy.orderedPlaylistForCategory(
            allAudios = listOf(m2, q1, m1),
            targetType = AudioTargetType.MUTUN,
            minHafalanSurah = 2,
            maxHafalanSurah = 9
        )

        assertEquals(listOf(20L, 21L), orderedMutun.map { it.id })
        assertTrue(SequentialLockPolicy.isUnlocked(orderedMutun, 0, emptySet()))
        assertFalse(SequentialLockPolicy.isUnlocked(orderedMutun, 1, emptySet()))

        // Completing m1 unlocks m2 even if q1 is incomplete
        assertTrue(SequentialLockPolicy.isUnlocked(orderedMutun, 1, setOf(20L)))
    }
}
