package com.hafalan.quranmutun.data.seed

import com.hafalan.quranmutun.data.model.AudioTargetType
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranicAudioCatalogTest {

    @Test
    fun `generates 114 surah audio entries for qari 5`() {
        val entries = QuranicAudioCatalog.qari5Entries()
        assertEquals(114, entries.size)
        assertEquals((1..114).toList(), entries.map { it.surahNumber })
        entries.forEach { entry ->
            assertEquals(AudioTargetType.QURAN, entry.targetType)
        }
        assertEquals(
            "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/001.mp3",
            entries.first().streamUrl
        )
        assertEquals(
            "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/009.mp3",
            entries[8].streamUrl
        )
        assertEquals(
            "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/114.mp3",
            entries.last().streamUrl
        )
    }
}
